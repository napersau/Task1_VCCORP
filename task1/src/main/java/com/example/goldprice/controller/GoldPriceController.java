package com.example.goldprice.controller;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.dto.HealthResponse;
import com.example.goldprice.service.GoldPriceService;
import java.time.Instant;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gold-prices")
@Validated
public class GoldPriceController {

    private static final Logger log = LoggerFactory.getLogger(GoldPriceController.class);

    private final GoldPriceService goldPriceService;

    public GoldPriceController(GoldPriceService goldPriceService) {
        this.goldPriceService = goldPriceService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        log.debug("Health check requested");
        return new HealthResponse("ok", "gold-price-api", Instant.now());
    }

    @GetMapping
    public List<GoldPriceResponse> getGoldPrices() {
        log.info("Fetching all gold prices");
        return goldPriceService.getGoldPrices();
    }

    @GetMapping("/{goldType}")
    public List<GoldPriceResponse> getGoldPriceByType(
            @PathVariable
            @NotBlank(message = "goldType must not be blank")
            @Pattern(regexp = "^[A-Za-z0-9]+$", message = "goldType must be alphanumeric")
            String goldType) {
        log.info("Fetching gold prices for type={}", goldType);
        return goldPriceService.getGoldPricesByType(goldType);
    }
}
