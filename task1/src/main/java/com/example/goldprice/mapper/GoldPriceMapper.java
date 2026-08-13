package com.example.goldprice.mapper;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.model.GoldPrice;
import org.mapstruct.Mapper;

@Mapper
public interface GoldPriceMapper {

    GoldPriceResponse toResponse(GoldPrice price);
}
