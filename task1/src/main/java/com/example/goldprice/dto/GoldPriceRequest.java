package com.example.goldprice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record GoldPriceRequest(
        @NotBlank(message = "Loại vàng không được để trống")
        @Size(max = 50, message = "Loại vàng không được vượt quá 50 ký tự")
        @Pattern(regexp = "^[\\p{L}0-9 ._-]+$", message = "Loại vàng chứa ký tự không hợp lệ")
        String goldType,

        @NotNull(message = "Giá mua không được để trống")
        @DecimalMin(value = "0", inclusive = true, message = "Giá mua phải lớn hơn hoặc bằng 0")
        @Digits(integer = 17, fraction = 2, message = "Giá mua không đúng định dạng")
        BigDecimal buyPrice,

        @NotNull(message = "Giá bán không được để trống")
        @DecimalMin(value = "0", inclusive = true, message = "Giá bán phải lớn hơn hoặc bằng 0")
        @Digits(integer = 17, fraction = 2, message = "Giá bán không đúng định dạng")
        BigDecimal sellPrice
) {
}
