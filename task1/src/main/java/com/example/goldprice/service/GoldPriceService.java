package com.example.goldprice.service;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import com.example.goldprice.mapper.GoldPriceMapper;
import com.example.goldprice.repository.GoldPriceRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GoldPriceService {

    private final GoldPriceRepository goldPriceRepository;
    private final GoldPriceMapper goldPriceMapper;

    public GoldPriceService(GoldPriceRepository goldPriceRepository, GoldPriceMapper goldPriceMapper) {
        this.goldPriceRepository = goldPriceRepository;
        this.goldPriceMapper = goldPriceMapper;
    }

    public List<GoldPriceResponse> getGoldPrices() {
        var prices = goldPriceRepository.findAllByOrderByGoldTypeAsc();

        if (prices.isEmpty()) {
            throw new GoldPriceNotFoundException("Khong tim thay du lieu gia vang hien tai");
        }

        return prices.stream()
                .map(goldPriceMapper::toResponse)
                .toList();
    }

    public List<GoldPriceResponse> getGoldPricesByType(String goldType) {
        var prices = goldPriceRepository.findByGoldTypeIgnoreCaseOrderByUpdatedAtDesc(goldType);

        if (prices.isEmpty()) {
            throw new GoldPriceNotFoundException("Khong tim thay du lieu gia vang cho loai: " + goldType);
        }

        return prices.stream()
                .map(goldPriceMapper::toResponse)
                .toList();
    }
}
