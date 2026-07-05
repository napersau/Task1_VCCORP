package com.example.goldprice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.goldprice.GoldPriceApiApplication;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GoldPriceApiApplication.class)
class GoldPriceServiceTest {

    @Autowired
    private GoldPriceService goldPriceService;

    @Test
    void getGoldPricesReturnsSeededPrices() {
        var prices = goldPriceService.getGoldPrices();

        assertThat(prices).hasSize(5);
        assertThat(prices.getFirst().goldType()).isEqualTo("14K");
        assertThat(prices.getLast().goldType()).isEqualTo("SJC");
    }

    @Test
    void getGoldPricesByTypeIsCaseInsensitive() {
        var prices = goldPriceService.getGoldPricesByType("sjc");

        assertThat(prices).hasSize(1);
        assertThat(prices).allMatch(price -> price.goldType().equals("SJC"));
    }

    @Test
    void getGoldPricesThrowsExceptionWhenNoDataExists() {
        assertThatThrownBy(() -> goldPriceService.getGoldPricesByType("gold-that-does-not-exist"))
                .isInstanceOf(GoldPriceNotFoundException.class);
    }
}
