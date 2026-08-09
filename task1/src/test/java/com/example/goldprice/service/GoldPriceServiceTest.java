package com.example.goldprice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.goldprice.dto.GoldPriceRequest;
import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.exception.GoldPriceNotFoundException;
import com.example.goldprice.mapper.GoldPriceMapper;
import com.example.goldprice.model.GoldPrice;
import com.example.goldprice.repository.GoldPriceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoldPriceServiceTest {

    @Mock private GoldPriceRepository repository;
    @Mock private GoldPriceMapper mapper;
    @InjectMocks private GoldPriceService service;

    @Test
    void createNormalizesTypeAndSavesEntity() {
        var request = request("  sjc  ", "80000000", "82000000");
        var response = response(1L, "SJC", "80000000", "82000000");
        when(repository.save(any(GoldPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(GoldPrice.class))).thenReturn(response);

        assertThat(service.create(request)).isEqualTo(response);

        ArgumentCaptor<GoldPrice> captor = ArgumentCaptor.forClass(GoldPrice.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getGoldType()).isEqualTo("SJC");
    }

    @Test
    void updateChangesPricesWhenRecordExists() {
        GoldPrice entity = new GoldPrice("SJC", new BigDecimal("80000000"), new BigDecimal("82000000"));
        var request = request("SJC", "81000000", "83000000");
        var response = response(1L, "SJC", "81000000", "83000000");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(1L, request).buyPrice()).isEqualByComparingTo("81000000");
        assertThat(entity.getBuyPrice()).isEqualByComparingTo("81000000");
    }

    @Test
    void rejectsInvalidPriceAndMissingRecord() {
        assertThatThrownBy(() -> service.create(request("SJC", "100", "90")))
                .isInstanceOf(IllegalArgumentException.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(GoldPriceNotFoundException.class);
    }

    private GoldPriceRequest request(String type, String buy, String sell) {
        return new GoldPriceRequest(type, new BigDecimal(buy), new BigDecimal(sell));
    }

    private GoldPriceResponse response(Long id, String type, String buy, String sell) {
        return new GoldPriceResponse(id, type, new BigDecimal(buy), new BigDecimal(sell), LocalDateTime.now());
    }
}
