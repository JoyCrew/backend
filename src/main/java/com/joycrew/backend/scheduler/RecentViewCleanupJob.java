package com.joycrew.backend.scheduler;

import com.joycrew.backend.service.RecentProductViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecentViewCleanupJob {

    private final RecentProductViewService recentProductViewService;

    // 매일 새벽 03:00 (서울 시간대)
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanupOldRecentViews() {
        long deleted = recentProductViewService.cleanupOldViews();
        if (deleted > 0) {
            log.info("Cleaned up {} recent product views older than 3 months.", deleted);
        } else {
            log.debug("No recent product views to clean.");
        }
    }
}
