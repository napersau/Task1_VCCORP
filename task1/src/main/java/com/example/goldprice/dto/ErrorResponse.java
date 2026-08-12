package com.example.goldprice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        String requestId,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
}
