package com.joycrew.backend.service;

import com.joycrew.backend.dto.kakao.ExternalProductDetailResponse;
import com.joycrew.backend.dto.kakao.ExternalProductResponse;
import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.enums.SortOption;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import com.joycrew.backend.repository.KakaoTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalCatalogService {

    private final KakaoTemplateRepository templateRepo;

    @Value("${joycrew.points.krw_per_point:40}")
    private int krwPerPoint;

    public List<ExternalProductResponse> listByCategory(GiftCategory category, int page, int size, SortOption sort) {
        Sort s = switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "basePriceKrw");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "basePriceKrw");
            case POPULAR, NEW -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
        Page<KakaoTemplate> p = templateRepo.findByJoyCategory(category, PageRequest.of(page, size, s));

        return p.getContent().stream().map(t -> {
            int point = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);
            return new ExternalProductResponse(
                    t.getTemplateId(), t.getName(), point, t.getBasePriceKrw(), t.getThumbnailUrl()
            );
        }).toList();
    }

    public ExternalProductDetailResponse getDetailWithPoints(String templateId) {
        var t = templateRepo.findById(templateId).orElse(null);
        if (t == null) return null;

        int basePoint = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);

        return new ExternalProductDetailResponse(
                t.getTemplateId(),
                t.getName(),
                basePoint,
                t.getBasePriceKrw(),
                t.getThumbnailUrl()
        );
    }
}
