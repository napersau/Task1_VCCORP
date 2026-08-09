package com.example.goldprice.integration;

import java.util.List;

public interface GoldPriceSourceClient {

    List<GoldPriceFeedItem> fetchPrices();
}
