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
@Table(name = "gold_prices")
public class GoldPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String goldType;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal buyPrice;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal sellPrice;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected GoldPrice() {
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
