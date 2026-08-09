package com.example.goldprice.scheduler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.goldprice.integration.GoldPriceFeedItem;
import com.example.goldprice.integration.GoldPriceSourceClient;
import com.example.goldprice.service.GoldPriceSynchronizationService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoldPriceSchedulerTest {

    @Mock private GoldPriceSourceClient sourceClient;
    @Mock private GoldPriceSynchronizationService synchronizationService;
    @InjectMocks private GoldPriceScheduler scheduler;

    @Test
    void fetchesAndPersistsPrices() {
        var prices = List.of(new GoldPriceFeedItem(
                "SJC", new BigDecimal("80000000"), new BigDecimal("82000000")));
        when(sourceClient.fetchPrices()).thenReturn(prices);

        scheduler.synchronize();

        verify(synchronizationService).saveNewPrices(prices);
    }

    @Test
    void sourceFailureDoesNotStopFutureScheduledRuns() {
        when(sourceClient.fetchPrices()).thenThrow(new RuntimeException("source unavailable"));
        assertThatCode(scheduler::synchronize).doesNotThrowAnyException();
    }
}
