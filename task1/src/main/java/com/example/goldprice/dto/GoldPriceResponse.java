package com.example.goldprice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GoldPriceResponse(
        Long id,
        String goldType,
        BigDecimal buyPrice,
        BigDecimal sellPrice,
        LocalDateTime updatedAt
) implements Serializable {
}
