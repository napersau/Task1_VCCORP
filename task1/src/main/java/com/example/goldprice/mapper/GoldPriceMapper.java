package com.example.goldprice.mapper;

import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.model.GoldPrice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GoldPriceMapper {

    GoldPriceResponse toResponse(GoldPrice price);
}
