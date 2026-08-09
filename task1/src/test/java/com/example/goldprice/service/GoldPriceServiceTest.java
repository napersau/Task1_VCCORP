package com.example.goldprice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.goldprice.dto.GoldPriceRequest;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:service-test;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class GoldPriceServiceTest {

    @Autowired
    private GoldPriceService service;

    @Test
    void performsCrudAndCaseInsensitiveSearch() {
        var created = service.create(request("sjc", "80000000", "82000000"));
        assertThat(created.id()).isNotNull();
        assertThat(created.goldType()).isEqualTo("SJC");

        var result = service.search("Sj", PageRequest.of(0, 10));
        assertThat(result.getContent()).extracting("id").containsExactly(created.id());

        var updated = service.update(created.id(), request("sjc", "81000000", "83000000"));
        assertThat(updated.buyPrice()).isEqualByComparingTo("81000000");

        service.delete(created.id());
        assertThatThrownBy(() -> service.getById(created.id()))
                .isInstanceOf(GoldPriceNotFoundException.class);
    }

    @Test
    void rejectsSellPriceLowerThanBuyPrice() {
        assertThatThrownBy(() -> service.create(request("SJC", "82000000", "80000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Giá bán");
    }

    private GoldPriceRequest request(String type, String buy, String sell) {
        return new GoldPriceRequest(type, new BigDecimal(buy), new BigDecimal(sell));
    }
}
