package com.example.goldprice.service;

import com.example.goldprice.dto.GoldPriceRequest;
import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import com.example.goldprice.mapper.GoldPriceMapper;
import com.example.goldprice.model.GoldPrice;
import com.example.goldprice.repository.GoldPriceRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoldPriceService {

    private final GoldPriceRepository goldPriceRepository;
    private final GoldPriceMapper goldPriceMapper;

    public GoldPriceService(GoldPriceRepository goldPriceRepository, GoldPriceMapper goldPriceMapper) {
        this.goldPriceRepository = goldPriceRepository;
        this.goldPriceMapper = goldPriceMapper;
    }

    @Transactional(readOnly = true)
    public Page<GoldPriceResponse> search(String goldType, Pageable pageable) {
        Page<GoldPrice> prices = goldType == null || goldType.isBlank()
                ? goldPriceRepository.findAll(pageable)
                : goldPriceRepository.findByGoldTypeContainingIgnoreCase(goldType.trim(), pageable);
        return prices.map(goldPriceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public GoldPriceResponse getById(Long id) {
        return goldPriceMapper.toResponse(findById(id));
    }

    @Transactional
    public GoldPriceResponse create(GoldPriceRequest request) {
        validatePriceRange(request);
        GoldPrice price = new GoldPrice(normalizeType(request.goldType()), request.buyPrice(), request.sellPrice());
        return goldPriceMapper.toResponse(goldPriceRepository.save(price));
    }

    @Transactional
    public GoldPriceResponse update(Long id, GoldPriceRequest request) {
        validatePriceRange(request);
        GoldPrice price = findById(id);
        price.update(normalizeType(request.goldType()), request.buyPrice(), request.sellPrice());
        return goldPriceMapper.toResponse(goldPriceRepository.save(price));
    }

    @Transactional
    public void delete(Long id) {
        GoldPrice price = findById(id);
        goldPriceRepository.delete(price);
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
