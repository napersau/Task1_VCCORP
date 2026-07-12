package com.example.goldprice.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.goldprice.GoldPriceApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = GoldPriceApiApplication.class)
@AutoConfigureMockMvc
class GoldPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getGoldPricesReturnsSeededList() throws Exception {
        mockMvc.perform(get("/api/gold-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].goldType").value("14K"))
                .andExpect(jsonPath("$[4].goldType").value("SJC"))
                .andExpect(jsonPath("$[4].buyPrice").value(80000000))
                .andExpect(jsonPath("$[4].sellPrice").value(82000000));
    }

    @Test
    void getGoldPricesByTypeReturnsTypeHistory() throws Exception {
        mockMvc.perform(get("/api/gold-prices/sjc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].goldType").value("SJC"))
                .andExpect(jsonPath("$[0].updatedAt").value("2026-07-02T09:00:00"));
    }

    @Test
    void getGoldPricesByMissingTypeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/gold-prices/abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void getGoldPricesByInvalidTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/gold-prices/sjc!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Du lieu dau vao khong hop le"));
    }
}
