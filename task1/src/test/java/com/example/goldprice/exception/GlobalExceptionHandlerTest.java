package com.example.goldprice.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.goldprice.config.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/gold-prices");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE)).thenReturn("test-request-id");
    }

    @Test
    void mapsDatabaseFailureToSafeServiceUnavailableResponse() {
        var response = handler.handleDatabaseFailure(
                new DataAccessResourceFailureException("jdbc:password=secret"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("DATABASE_UNAVAILABLE");
        assertThat(response.getBody().message()).doesNotContain("secret");
    }

    @Test
    void mapsRedisFailureToSafeServiceUnavailableResponse() {
        var response = handler.handleRedisFailure(
                new RedisConnectionFailureException("redis password secret"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("CACHE_UNAVAILABLE");
        assertThat(response.getBody().requestId()).isEqualTo("test-request-id");
    }
}
