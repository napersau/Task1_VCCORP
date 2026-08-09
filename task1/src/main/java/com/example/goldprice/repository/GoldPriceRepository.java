package com.example.goldprice.repository;

import com.example.goldprice.model.GoldPrice;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoldPriceRepository extends JpaRepository<GoldPrice, Long> {

    Page<GoldPrice> findByGoldTypeContainingIgnoreCase(String goldType, Pageable pageable);

    boolean existsByGoldTypeIgnoreCaseAndBuyPriceAndSellPrice(
            String goldType, BigDecimal buyPrice, BigDecimal sellPrice);
}
