package com.example.goldprice.controller;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.dto.HealthResponse;
import com.example.goldprice.service.GoldPriceService;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gold-prices")
public class GoldPriceController {

    private final GoldPriceService goldPriceService;

    public GoldPriceController(GoldPriceService goldPriceService) {
        this.goldPriceService = goldPriceService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", "gold-price-api", Instant.now());
    }

    @GetMapping
    public List<GoldPriceResponse> getGoldPrices() {
        return goldPriceService.getGoldPrices();
    }

    @GetMapping("/{goldType}")
    public List<GoldPriceResponse> getGoldPriceByType(@PathVariable String goldType) {
        return goldPriceService.getGoldPricesByType(goldType);
    }
}
