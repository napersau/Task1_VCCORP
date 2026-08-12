package com.example.goldprice.service;

import com.example.goldprice.dto.GoldPriceRequest;
import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import com.example.goldprice.mapper.GoldPriceMapper;
import com.example.goldprice.model.GoldPrice;
import com.example.goldprice.repository.GoldPriceRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoldPriceService {

    private static final Logger log = LoggerFactory.getLogger(GoldPriceService.class);

    private final GoldPriceRepository goldPriceRepository;
    private final GoldPriceMapper goldPriceMapper;

    public GoldPriceService(GoldPriceRepository goldPriceRepository, GoldPriceMapper goldPriceMapper) {
        this.goldPriceRepository = goldPriceRepository;
        this.goldPriceMapper = goldPriceMapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "goldPrices",
            key = "{#goldType, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<GoldPriceResponse> search(String goldType, Pageable pageable) {
        log.debug("Searching gold prices: goldType={}, page={}, size={}, sort={}",
                goldType, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<GoldPrice> prices = goldType == null || goldType.isBlank()
                ? goldPriceRepository.findAll(pageable)
                : goldPriceRepository.findByGoldTypeContainingIgnoreCase(goldType.trim(), pageable);
        log.debug("Gold price search completed: totalElements={}", prices.getTotalElements());
        return prices.map(goldPriceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "goldPriceById", key = "#id")
    public GoldPriceResponse getById(Long id) {
        log.debug("Fetching gold price: id={}", id);
        return goldPriceMapper.toResponse(findById(id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "goldPrices", allEntries = true),
            @CacheEvict(cacheNames = "goldPriceById", allEntries = true)
    })
    public GoldPriceResponse create(GoldPriceRequest request) {
        validatePriceRange(request);
        GoldPrice price = new GoldPrice(normalizeType(request.goldType()), request.buyPrice(), request.sellPrice());
        GoldPrice saved = goldPriceRepository.save(price);
        log.info("Gold price created: id={}, goldType={}", saved.getId(), saved.getGoldType());
        return goldPriceMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "goldPrices", allEntries = true),
            @CacheEvict(cacheNames = "goldPriceById", allEntries = true)
    })
    public GoldPriceResponse update(Long id, GoldPriceRequest request) {
        validatePriceRange(request);
        GoldPrice price = findById(id);
        price.update(normalizeType(request.goldType()), request.buyPrice(), request.sellPrice());
        GoldPrice saved = goldPriceRepository.save(price);
        log.info("Gold price updated: id={}, goldType={}", saved.getId(), saved.getGoldType());
        return goldPriceMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "goldPrices", allEntries = true),
            @CacheEvict(cacheNames = "goldPriceById", allEntries = true)
    })
    public void delete(Long id) {
        GoldPrice price = findById(id);
        goldPriceRepository.delete(price);
        log.info("Gold price deleted: id={}, goldType={}", id, price.getGoldType());
    }

    private GoldPrice findById(Long id) {
        return goldPriceRepository.findById(id)
                .orElseThrow(() -> new GoldPriceNotFoundException("Không tìm thấy giá vàng có id: " + id));
    }

    private String normalizeType(String goldType) {
        return goldType.trim().toUpperCase(Locale.ROOT);
    }

    private void validatePriceRange(GoldPriceRequest request) {
        if (request.sellPrice().compareTo(request.buyPrice()) < 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn hoặc bằng giá mua");
        }
    }
}
