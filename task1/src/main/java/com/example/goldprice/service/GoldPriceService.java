package com.example.goldprice.service;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import com.example.goldprice.mapper.GoldPriceMapper;
import com.example.goldprice.repository.GoldPriceRepository;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public List<GoldPriceResponse> getGoldPrices() {
        var prices = goldPriceRepository.findAllByOrderByGoldTypeAsc();

        if (prices.isEmpty()) {
            throw new GoldPriceNotFoundException("Khong tim thay du lieu gia vang hien tai");
        }

        log.debug("Found {} gold price records", prices.size());
        return prices.stream()
                .map(goldPriceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GoldPriceResponse> getGoldPricesByType(String goldType) {
        var normalizedGoldType = goldType == null ? null : goldType.trim().toUpperCase(Locale.ROOT);
        var prices = goldPriceRepository.findByGoldTypeIgnoreCaseOrderByUpdatedAtDesc(normalizedGoldType);

        if (prices.isEmpty()) {
            throw new GoldPriceNotFoundException("Khong tim thay du lieu gia vang cho loai: " + normalizedGoldType);
        }

        log.debug("Found {} gold price records for type={}", prices.size(), normalizedGoldType);
        return prices.stream()
                .map(goldPriceMapper::toResponse)
                .toList();
    }
}
