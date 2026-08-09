package com.example.goldprice.controller;

import com.example.goldprice.dto.GoldPriceRequest;
import com.example.goldprice.dto.GoldPriceResponse;
import com.example.goldprice.dto.HealthResponse;
import com.example.goldprice.dto.PageResponse;
import com.example.goldprice.service.GoldPriceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gold-prices")
@Validated
public class GoldPriceController {

    private final GoldPriceService goldPriceService;

    public GoldPriceController(GoldPriceService goldPriceService) {
        this.goldPriceService = goldPriceService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", "gold-price-api", Instant.now());
    }

    @GetMapping
    public PageResponse<GoldPriceResponse> search(
            @RequestParam(required = false) String goldType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "updatedAt")
            @Pattern(regexp = "^(id|goldType|buyPrice|sellPrice|updatedAt)$", message = "Trường sắp xếp không hợp lệ")
            String sortBy,
            @RequestParam(defaultValue = "desc")
            @Pattern(regexp = "(?i)^(asc|desc)$", message = "Chiều sắp xếp phải là asc hoặc desc")
            String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return PageResponse.from(goldPriceService.search(goldType, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public GoldPriceResponse getById(@PathVariable @Min(1) Long id) {
        return goldPriceService.getById(id);
    }

    @PostMapping
    public ResponseEntity<GoldPriceResponse> create(@Valid @RequestBody GoldPriceRequest request) {
        GoldPriceResponse created = goldPriceService.create(request);
        return ResponseEntity.created(URI.create("/api/gold-prices/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public GoldPriceResponse update(@PathVariable @Min(1) Long id,
                                    @Valid @RequestBody GoldPriceRequest request) {
        return goldPriceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        goldPriceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
