package com.example.goldprice.scheduler;

import com.example.goldprice.integration.GoldPriceSourceClient;
import com.example.goldprice.service.GoldPriceSynchronizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "gold-price.scheduler.enabled", havingValue = "true")
public class GoldPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(GoldPriceScheduler.class);

    private final GoldPriceSourceClient sourceClient;
    private final GoldPriceSynchronizationService synchronizationService;

    public GoldPriceScheduler(GoldPriceSourceClient sourceClient,
                              GoldPriceSynchronizationService synchronizationService) {
        this.sourceClient = sourceClient;
        this.synchronizationService = synchronizationService;
    }

    @Scheduled(cron = "${gold-price.scheduler.cron:0 */5 * * * *}",
            zone = "${gold-price.scheduler.zone:Asia/Ho_Chi_Minh}")
    public void synchronize() {
        try {
            int inserted = synchronizationService.saveNewPrices(sourceClient.fetchPrices());
            log.info("Gold price synchronization completed: {} new record(s)", inserted);
        } catch (Exception exception) {
            log.error("Gold price synchronization failed; the next scheduled run will retry", exception);
        }
    }
}
