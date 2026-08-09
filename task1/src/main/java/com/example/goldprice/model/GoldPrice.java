package com.example.goldprice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gold_price")
public class GoldPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gold_type", nullable = false, length = 50)
    private String goldType;

    @Column(name = "buy_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal buyPrice;

    @Column(name = "sell_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal sellPrice;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected GoldPrice() {
    }

    public GoldPrice(String goldType, BigDecimal buyPrice, BigDecimal sellPrice) {
        update(goldType, buyPrice, sellPrice);
    }

    public void update(String goldType, BigDecimal buyPrice, BigDecimal sellPrice) {
        this.goldType = goldType;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getGoldType() {
        return goldType;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
