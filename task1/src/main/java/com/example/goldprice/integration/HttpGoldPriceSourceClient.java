package com.example.goldprice.integration;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "gold-price.scheduler.enabled", havingValue = "true")
public class HttpGoldPriceSourceClient implements GoldPriceSourceClient {

    private final WebClient webClient;
    private final String sourceUrl;
    private final Duration timeout;

    public HttpGoldPriceSourceClient(WebClient.Builder builder,
                                     @Value("${gold-price.scheduler.source-url}") String sourceUrl,
                                     @Value("${gold-price.scheduler.timeout:10s}") Duration timeout,
                                     @Value("${gold-price.scheduler.api-key:}") String apiKey) {
        WebClient.Builder configuredBuilder = builder.clone();
        if (!apiKey.isBlank()) {
            configuredBuilder.defaultHeader("X-API-Key", apiKey);
        }
        this.webClient = configuredBuilder.build();
        this.sourceUrl = sourceUrl;
        this.timeout = timeout;
    }

    @Override
    public List<GoldPriceFeedItem> fetchPrices() {
        List<GoldPriceFeedItem> result = webClient.get()
                .uri(sourceUrl)
                .retrieve()
                .bodyToFlux(GoldPriceFeedItem.class)
                .collectList()
                .block(timeout);
        return result == null ? List.of() : result;
    }
}
