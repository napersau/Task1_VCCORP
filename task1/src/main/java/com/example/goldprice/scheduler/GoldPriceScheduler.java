package com.example.goldprice.scheduler;

import com.example.goldprice.exception.GoldPriceSourceException;
import com.example.goldprice.integration.GoldPriceSourceClient;
import com.example.goldprice.service.GoldPriceSynchronizationService;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
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
        Instant startedAt = Instant.now();
        log.info("Gold price synchronization started");
        try {
            var prices = sourceClient.fetchPrices();
            int inserted = synchronizationService.saveNewPrices(prices);
            log.info("Gold price synchronization completed: fetched={}, inserted={}, durationMs={}",
                    prices.size(), inserted, elapsedMillis(startedAt));
        } catch (GoldPriceSourceException exception) {
            log.error("Gold price source unavailable: durationMs={}, reason={}",
                    elapsedMillis(startedAt), exception.getMessage(), exception);
        } catch (RedisConnectionFailureException exception) {
            log.error("Redis connection failed after gold price synchronization: durationMs={}",
                    elapsedMillis(startedAt), exception);
        } catch (DataAccessException exception) {
            log.error("Database operation failed during gold price synchronization: durationMs={}",
                    elapsedMillis(startedAt), exception);
        } catch (Exception exception) {
            log.error("Unexpected gold price synchronization failure: durationMs={}",
                    elapsedMillis(startedAt), exception);
        }
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }
}
