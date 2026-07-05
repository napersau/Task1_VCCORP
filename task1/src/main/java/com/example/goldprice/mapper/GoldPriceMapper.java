package com.example.goldprice.mapper;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.model.GoldPrice;
import org.springframework.stereotype.Component;

@Component
public class GoldPriceMapper {

    public GoldPriceResponse toResponse(GoldPrice price) {
        return new GoldPriceResponse(
                price.getGoldType(),
                price.getBuyPrice(),
                price.getSellPrice(),
                price.getUpdatedAt()
        );
    }
}
