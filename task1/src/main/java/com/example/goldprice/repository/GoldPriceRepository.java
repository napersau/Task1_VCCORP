package com.example.goldprice.repository;

import com.example.goldprice.model.GoldPrice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoldPriceRepository extends JpaRepository<GoldPrice, Long> {

        List<GoldPrice> findAllByOrderByGoldTypeAsc();

        List<GoldPrice> findByGoldTypeIgnoreCaseOrderByUpdatedAtDesc(String goldType);
}
