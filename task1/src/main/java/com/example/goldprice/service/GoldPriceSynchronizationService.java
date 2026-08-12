package com.example.goldprice.service;

import com.example.goldprice.integration.GoldPriceFeedItem;
import com.example.goldprice.model.GoldPrice;
import com.example.goldprice.repository.GoldPriceRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoldPriceSynchronizationService {

    private static final Logger log = LoggerFactory.getLogger(GoldPriceSynchronizationService.class);

    private final GoldPriceRepository repository;

    public GoldPriceSynchronizationService(GoldPriceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "goldPrices", allEntries = true, condition = "#result > 0"),
            @CacheEvict(cacheNames = "goldPriceById", allEntries = true, condition = "#result > 0")
    })
    public int saveNewPrices(List<GoldPriceFeedItem> sourceItems) {
        if (sourceItems == null || sourceItems.isEmpty()) {
            log.info("No gold prices received from source");
            return 0;
        }
        // Normalize type and numeric scale first so duplicates in different source formats share one key.
        Map<String, GoldPriceFeedItem> uniqueItems = new LinkedHashMap<>();
        sourceItems.stream().filter(this::isValid).forEach(item -> {
            String type = item.goldType().trim().toUpperCase(Locale.ROOT);
            String key = type + '|' + item.buyPrice().stripTrailingZeros() + '|' + item.sellPrice().stripTrailingZeros();
            uniqueItems.putIfAbsent(key, new GoldPriceFeedItem(type, item.buyPrice(), item.sellPrice()));
        });

        List<GoldPrice> newPrices = uniqueItems.values().stream()
                .filter(item -> !repository.existsByGoldTypeIgnoreCaseAndBuyPriceAndSellPrice(
                        item.goldType(), item.buyPrice(), item.sellPrice()))
                .map(item -> new GoldPrice(item.goldType(), item.buyPrice(), item.sellPrice()))
                .toList();
        repository.saveAll(newPrices);
        log.info("Gold prices processed: received={}, validUnique={}, inserted={}, ignored={}",
                sourceItems.size(), uniqueItems.size(), newPrices.size(), sourceItems.size() - newPrices.size());
        return newPrices.size();
    }

    private boolean isValid(GoldPriceFeedItem item) {
        return item != null && item.goldType() != null && !item.goldType().isBlank()
                && item.buyPrice() != null && item.buyPrice().signum() >= 0
                && item.sellPrice() != null && item.sellPrice().compareTo(item.buyPrice()) >= 0;
    }
}
