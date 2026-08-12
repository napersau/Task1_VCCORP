package com.example.goldprice.exception;

import com.example.goldprice.config.RequestIdFilter;
import com.example.goldprice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GoldPriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            GoldPriceNotFoundException exception, HttpServletRequest request) {
        log.warn("Resource not found: path={}, reason={}", request.getRequestURI(), exception.getMessage());
        return response(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotFound(
            NoResourceFoundException exception, HttpServletRequest request) {
        log.warn("Endpoint not found: method={}, path={}", request.getMethod(), request.getRequestURI());
        return response(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "Không tìm thấy endpoint được yêu cầu", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        log.warn("Request body validation failed: path={}, fields={}",
                request.getRequestURI(), fieldErrors.keySet());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Dữ liệu đầu vào không hợp lệ", request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String property = violation.getPropertyPath().toString();
            String field = property.substring(property.lastIndexOf('.') + 1);
            fieldErrors.putIfAbsent(field, violation.getMessage());
        });
        log.warn("Request parameter validation failed: path={}, fields={}",
                request.getRequestURI(), fieldErrors.keySet());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Tham số yêu cầu không hợp lệ", request, fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "Tham số '" + exception.getName() + "' không đúng kiểu dữ liệu", request, Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "Thiếu tham số bắt buộc: " + exception.getParameterName(), request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.warn("Malformed JSON request: path={}", request.getRequestURI());
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "Nội dung JSON không hợp lệ", request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(
            IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Business validation failed: path={}, reason={}", request.getRequestURI(), exception.getMessage());
        return response(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION",
                exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Phương thức HTTP không được hỗ trợ", request, Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type không được hỗ trợ", request, Map.of());
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<ErrorResponse> handleRedisFailure(
            RedisConnectionFailureException exception, HttpServletRequest request) {
        log.error("Redis connection failed: path={}", request.getRequestURI(), exception);
        return response(HttpStatus.SERVICE_UNAVAILABLE, "CACHE_UNAVAILABLE",
                "Dịch vụ bộ nhớ đệm tạm thời không khả dụng", request, Map.of());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseFailure(
            DataAccessException exception, HttpServletRequest request) {
        log.error("Database operation failed: path={}", request.getRequestURI(), exception);
        return response(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_UNAVAILABLE",
                "Dịch vụ dữ liệu tạm thời không khả dụng", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        log.error("Unexpected server error: method={}, path={}",
                request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Hệ thống đang gặp lỗi, vui lòng thử lại sau", request, Map.of());
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String message,
                                                   HttpServletRequest request, Map<String, String> fieldErrors) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        ErrorResponse body = new ErrorResponse(status.value(), error, message, request.getRequestURI(),
                requestId == null ? null : requestId.toString(), Instant.now(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
