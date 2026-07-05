package com.example.goldprice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GoldPriceResponse(
        String goldType,
        BigDecimal buyPrice,
        BigDecimal sellPrice,
        LocalDateTime updatedAt
) {
}
