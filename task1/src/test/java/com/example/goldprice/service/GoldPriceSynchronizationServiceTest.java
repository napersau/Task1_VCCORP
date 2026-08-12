package com.example.goldprice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.goldprice.integration.GoldPriceFeedItem;
import com.example.goldprice.model.GoldPrice;
import com.example.goldprice.repository.GoldPriceRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoldPriceSynchronizationServiceTest {

    @Mock private GoldPriceRepository repository;

    @Test
    void filtersInvalidAndDuplicatePricesBeforeSaving() {
        when(repository.existsByGoldTypeIgnoreCaseAndBuyPriceAndSellPrice(
                any(), any(), any())).thenReturn(false);
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new GoldPriceSynchronizationService(repository);
        var sjc = item("sjc", "80000000", "82000000");

        int inserted = service.saveNewPrices(List.of(
                sjc,
                item("SJC", "80000000.00", "82000000.0"),
                item("invalid", "100", "90")));

        assertThat(inserted).isEqualTo(1);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GoldPrice>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement()
                .extracting(GoldPrice::getGoldType).isEqualTo("SJC");
    }

    @Test
    void acceptsNullOrEmptySourceWithoutCallingDatabase() {
        var service = new GoldPriceSynchronizationService(repository);

        assertThat(service.saveNewPrices(null)).isZero();
        assertThat(service.saveNewPrices(List.of())).isZero();
    }

    private GoldPriceFeedItem item(String type, String buy, String sell) {
        return new GoldPriceFeedItem(type, new BigDecimal(buy), new BigDecimal(sell));
    }
}
