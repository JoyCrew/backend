package com.joycrew.backend.controller;

import com.joycrew.backend.dto.IssueBillingKeyRequest;
import com.joycrew.backend.dto.SubscriptionPaymentHistoryResponse;
import com.joycrew.backend.dto.SubscriptionSummaryResponse;
import com.joycrew.backend.dto.SuccessResponse;
import com.joycrew.backend.entity.enums.PaymentStatus;
import com.joycrew.backend.service.SubscriptionBillingKeyAppService;
import com.joycrew.backend.service.SubscriptionPaymentQueryService;
import com.joycrew.backend.service.SubscriptionQueryService;
import com.joycrew.backend.tenant.Tenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Subscription (Admin)",
        description = """
                회사 구독(정기결제) 관리 API.
                
                ### 전체 흐름(요약)
                1) 프론트에서 Toss BillingAuth 수행 후 authKey 획득  
                2) /billing-key/issue 로 authKey를 백엔드에 전달  
                3) 백엔드는 billingKey 발급/저장 및 autoRenew 활성화  
                4) 스케줄러가 만료일 도달 시 billingKey로 자동 결제 수행  
                5) 관리자는 /auto/disable 로 자동결제 해지 가능  
                """
)
@RestController
@RequestMapping("/api/admin/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionBillingKeyAppService billingKeyAppService;
    private final SubscriptionPaymentQueryService paymentQueryService;
    private final SubscriptionQueryService subscriptionQueryService;

    @Operation(
            summary = "BillingKey 발급 및 자동결제 활성화",
            description = """
                    Toss BillingAuth 성공 후 프론트에서 받은 **authKey**를 백엔드로 전달합니다.
                    
                    - 백엔드가 Toss에 authKey로 billingKey 발급 요청
                    - 성공 시 회사(Company)에 billingKey 저장 + autoRenew=true 활성화
                    - (선택) 첫 결제/구독 시작을 같이 처리하는 경우, 해당 서비스에서 추가로 결제 승인까지 수행할 수 있습니다.
                    
                    ✅ 관리자 페이지의 "카드 등록/자동결제 활성화" 버튼에 해당합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "billingKey 저장 및 자동결제 활성화 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Success",
                            value = """
                                    {
                                      "message": "BillingKey issued and auto-renew enabled"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "authKey 누락/형식 오류 등",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "BadRequest",
                            value = """
                                    {
                                      "message": "Invalid authKey"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/billing-key/issue")
    public ResponseEntity<SuccessResponse> issueBillingKey(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Toss BillingAuth 성공 후 전달되는 authKey",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "RequestExample",
                                    value = """
                                            {
                                              "authKey": "test_authKey_1234567890"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody IssueBillingKeyRequest req
    ) {
        Long companyId = Tenant.id();
        billingKeyAppService.issueAndSaveBillingKey(companyId, req.authKey());
        return ResponseEntity.ok(new SuccessResponse("BillingKey issued and auto-renew enabled"));
    }

    @Operation(
            summary = "구독 해지(자동결제 OFF)",
            description = """
                    자동결제만 해지합니다.  
                    - 이미 결제되어 남아있는 기간(subscriptionEndAt)까지는 정상 이용 가능
                    - 만료일 이후에는 자동 결제/연장이 수행되지 않습니다.
                    
                    ✅ 관리자 페이지의 "자동결제 해지" 버튼에 해당합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "자동결제 해지 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Success",
                            value = """
                                    {
                                      "message": "Auto-renew disabled"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/auto/disable")
    public ResponseEntity<SuccessResponse> disableAutoRenew() {
        Long companyId = Tenant.id();
        billingKeyAppService.disableAutoRenew(companyId);
        return ResponseEntity.ok(new SuccessResponse("Auto-renew disabled"));
    }

    @Operation(
            summary = "구독 결제 이력 조회(관리자): 구독 관리",
            description = """
                    회사의 구독 결제 이력을 페이지네이션 형태로 조회합니다.
                    
                    - status 파라미터로 결제 상태 필터링 가능
                    - 최신 결제부터 조회되도록(예: paidAt DESC) 구성하는 것을 권장합니다.
                    
                    ✅ 관리자 페이지의 "결제 내역" 화면에 해당합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "결제 이력 조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "HistoryExample",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 20,
                                      "totalPages": 3,
                                      "totalElements": 45,
                                      "items": [
                                        {
                                          "paymentId": 101,
                                          "orderId": "SUB_1_1700000000000",
                                          "amount": 50000,
                                          "status": "PAID",
                                          "paidAt": "2026-01-10T03:01:12"
                                        },
                                        {
                                          "paymentId": 100,
                                          "orderId": "SUB_1_1697320000000",
                                          "amount": 50000,
                                          "status": "FAILED",
                                          "paidAt": null
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/payments")
    public ResponseEntity<SubscriptionPaymentHistoryResponse> getPayments(
            @Parameter(
                    description = "페이지 번호(0부터 시작)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "페이지당 아이템 개수",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size,

            @Parameter(
                    description = "결제 상태 필터(선택). 예: PAID, FAILED, READY 등",
                    example = "PAID"
            )
            @RequestParam(required = false) PaymentStatus status
    ) {
        Long companyId = Tenant.id();
        return ResponseEntity.ok(paymentQueryService.getHistory(companyId, page, size, status));
    }

    @Operation(
            summary = "구독 요약 정보 조회: 구독 내역",
            description = """
                    현재 회사의 구독 상태 요약 정보를 조회합니다.
                    
                    일반적으로 다음 정보를 포함합니다:
                    - 현재 상태(ACTIVE/EXPIRED/PAYMENT_FAILED 등)
                    - subscriptionEndAt(만료일)
                    - autoRenew 여부
                    - 다음 결제 예정일(=subscriptionEndAt 기준)
                    
                    ✅ 관리자 페이지 상단의 "구독 상태 카드" 같은 요약 영역에 사용합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "구독 요약 조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "SummaryExample",
                            value = """
                                    {
                                      "companyId": 1,
                                      "status": "ACTIVE",
                                      "autoRenew": true,
                                      "subscriptionEndAt": "2026-02-10T23:59:59",
                                      "nextBillingAt": "2026-02-10T23:59:59",
                                      "monthlyPrice": 50000
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/summary")
    public ResponseEntity<SubscriptionSummaryResponse> getSubscriptionSummary() {
        Long companyId = Tenant.id();
        return ResponseEntity.ok(subscriptionQueryService.getSubscriptionSummary(companyId));
    }
}
