package com.joycrew.backend.service;

import com.joycrew.backend.dto.RecentViewedProductResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Product;
import com.joycrew.backend.entity.RecentProductView;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.ProductRepository;
import com.joycrew.backend.repository.RecentProductViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RecentProductViewService {

    private static final int DEFAULT_LIMIT = 20;

    private final RecentProductViewRepository recentProductViewRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;

    // Upsert viewed record
    @Transactional
    public void recordView(Long employeeId, Long productId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        LocalDateTime now = LocalDateTime.now();

        recentProductViewRepository.findByEmployee_EmployeeIdAndProduct_Id(employeeId, productId)
                .ifPresentOrElse(existing -> {
                    existing.setViewedAt(now);
                }, () -> {
                    RecentProductView view = RecentProductView.builder()
                            .employee(employee)
                            .product(product)
                            .viewedAt(now)
                            .build();
                    recentProductViewRepository.save(view);
                });
    }

    @Transactional(readOnly = true)
    public List<RecentViewedProductResponse> getRecentViews(Long employeeId, Integer limit) {
        int size = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, 100);
        LocalDateTime threshold = LocalDateTime.now().minus(3, ChronoUnit.MONTHS);

        var views = recentProductViewRepository
                .findByEmployee_EmployeeIdAndViewedAtAfterOrderByViewedAtDesc(
                        employeeId, threshold, PageRequest.of(0, size)
                );

        return views.stream()
                .map(v -> RecentViewedProductResponse.of(v.getProduct(), v.getViewedAt()))
                .toList();
    }

    // Called by a scheduler to keep the table small
    @Transactional
    public long cleanupOldViews() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);
        return recentProductViewRepository.deleteByViewedAtBefore(threshold);
    }
}
