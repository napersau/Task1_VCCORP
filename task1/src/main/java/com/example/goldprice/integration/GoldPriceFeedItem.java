package com.example.goldprice.integration;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

public record GoldPriceFeedItem(
        @JsonAlias({"gold_type", "type", "name"}) String goldType,
        @JsonAlias({"buy_price", "buy"}) BigDecimal buyPrice,
        @JsonAlias({"sell_price", "sell"}) BigDecimal sellPrice
) {
}
