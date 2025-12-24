package com.joycrew.backend.service;

import com.joycrew.backend.dto.CreateOrderRequest;
import com.joycrew.backend.dto.OrderResponse;
import com.joycrew.backend.dto.kakao.KakaoTemplateOrderRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Order;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.OrderStatus;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import com.joycrew.backend.exception.BillingRequiredException;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.kakao.KakaoGiftBizClient;
import com.joycrew.backend.repository.KakaoTemplateRepository;
import com.joycrew.backend.repository.OrderRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftPurchaseService {

    private final KakaoGiftBizClient kakao;
    private final WalletRepository walletRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final RewardPointTransactionRepository transactionRepository;
    private final KakaoTemplateRepository templateRepo;

    @Value("${joycrew.points.krw_per_point:40}")
    private int krwPerPoint;

    @Value("${kakao.callback.success-url:}")
    private String successCallbackUrl;
    @Value("${kakao.callback.fail-url:}")
    private String failCallbackUrl;
    @Value("${kakao.callback.gift-cancel-url:}")
    private String giftCancelCallbackUrl;

    @Value("${joycrew.kakao.dry-run:false}")
    private boolean dryRun;

    /**
     * 주문은 PENDING으로 먼저 저장 -> Kakao 호출 성공 시 PLACED, 실패 시 FAILED
     * 포인트는 선차감하고, 실패 시 환불하여 일관성 유지
     */
    @Transactional
    public OrderResponse purchaseWithPoints(Long employeeId, CreateOrderRequest req) {

        Long companyId = Tenant.id();

        // ✅ tenant 범위에서 employee 로드 + 회사 join fetch 권장(없으면 repository 메서드로)
        Employee employee = employeeRepository.findByCompanyCompanyIdAndEmployeeIdWithCompany(companyId, employeeId)
                .orElseThrow(() -> new UserNotFoundException("Employee not found"));

        Company company = employee.getCompany();

        // ✅ 카드등록 전이면 주문 차단
        if (!company.isBillingReady()) {
            throw new BillingRequiredException();
        }

        Wallet wallet = walletRepository.findByEmployee_EmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        KakaoTemplate template = templateRepo.findById(req.externalProductId())
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + req.externalProductId()));

        int qty = (req.quantity() == null || req.quantity() <= 0) ? 1 : req.quantity();
        int unitKrw = template.getBasePriceKrw();
        long totalKrw = (long) unitKrw * qty;
        int totalPoint = (int) Math.ceil(totalKrw / (double) krwPerPoint);

        String externalOrderId = buildExternalOrderId(employeeId, template.getTemplateId());

        Order order = Order.builder()
                .employee(employee)
                .productId(stableHashToLong(template.getTemplateId()))
                .productName(template.getName())
                .productUnitPrice((int) Math.ceil(unitKrw / (double) krwPerPoint))
                .quantity(qty)
                .totalPrice(totalPoint)
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .externalOrderId(externalOrderId)
                .build();
        order = orderRepository.save(order);

        wallet.purchaseWithPoints(totalPoint);

        String receiverPhone = Optional.ofNullable(employee.getPhoneNumber())
                .map(this::normalizePhone)
                .filter(s -> s != null && !s.isBlank())
                .orElseThrow(() -> new IllegalStateException("Employee has no phone number"));
        if (receiverPhone.length() < 8) {
            throw new IllegalStateException("Invalid receiver phone format: " + receiverPhone);
        }

        Map<String, Object> receiverObj = new HashMap<>();
        receiverObj.put("receiver_id", receiverPhone);

        KakaoTemplateOrderRequest kakaoReq = new KakaoTemplateOrderRequest(
                template.getTemplateToken(),
                "PHONE",
                List.of(receiverObj),
                emptyToNull(successCallbackUrl),
                emptyToNull(failCallbackUrl),
                emptyToNull(giftCancelCallbackUrl),
                template.getName(),
                externalOrderId
        );

        try {
            if (!dryRun) {
                String kakaoBody = kakao.sendTemplateOrder(kakaoReq);
                log.debug("[KAKAO] ORDER OK externalOrderId={} resBody={}", externalOrderId, kakaoBody);
            } else {
                log.warn("[DRY-RUN] Skipping Kakao call. Would send: {}", kakaoReq);
            }

            RewardPointTransaction tx = RewardPointTransaction.builder()
                    .sender(employee)
                    .receiver(null)
                    .pointAmount(totalPoint)
                    .message("KAKAO_SELF_PURCHASE:" + template.getTemplateId())
                    .type(TransactionType.REDEEM_ITEM)
                    .build();
            transactionRepository.save(tx);

            order.setStatus(OrderStatus.PLACED);
            order = orderRepository.save(order);

            return OrderResponse.from(order, template.getThumbnailUrl());

        } catch (ResponseStatusException ex) {
            refundWalletSilently(wallet, totalPoint);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw ex;

        } catch (RuntimeException ex) {
            refundWalletSilently(wallet, totalPoint);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw ex;
        }
    }

    private void refundWalletSilently(Wallet wallet, int totalPoint) {
        try {
            wallet.refundPoints(totalPoint);
        } catch (Exception e) {
            log.error("Failed to refund points on error (amount={}): {}", totalPoint, e.getMessage(), e);
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("\\D", "");
    }

    private String buildExternalOrderId(Long employeeId, String templateId) {
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "JC-" + employeeId + "-" + rand;
    }

    private long stableHashToLong(String s) {
        long h = 1469598103934665603L;
        for (byte b : s.getBytes()) { h ^= b; h *= 1099511628211L; }
        return h & Long.MAX_VALUE;
    }
}
