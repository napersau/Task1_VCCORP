package com.example.goldprice.config;

import com.example.goldprice.mapper.GoldPriceMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MapperConfig {

    @Bean
    GoldPriceMapper goldPriceMapper() {
        return Mappers.getMapper(GoldPriceMapper.class);
    }
}
