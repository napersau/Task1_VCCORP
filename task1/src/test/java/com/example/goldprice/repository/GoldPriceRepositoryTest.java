package com.example.goldprice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.goldprice.model.GoldPrice;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class GoldPriceRepositoryTest {

    @Autowired
    private GoldPriceRepository repository;

    @Test
    void savesAndSearchesByTypeIgnoringCaseWithPagination() {
        repository.save(new GoldPrice("SJC", new BigDecimal("80000000"), new BigDecimal("82000000")));
        repository.save(new GoldPrice("SJC NHẪN", new BigDecimal("79000000"), new BigDecimal("81000000")));
        repository.save(new GoldPrice("24K", new BigDecimal("78000000"), new BigDecimal("80000000")));

        var page = repository.findByGoldTypeContainingIgnoreCase("sjc", PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isNotNull();
    }
}
