package com.example.goldprice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.goldprice.repository.GoldPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:controller-test;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cache.type=none"
})
class GoldPriceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private GoldPriceRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    void supportsCrudSearchAndPagination() throws Exception {
        String body = "{\"goldType\":\"sjc\",\"buyPrice\":80000000,\"sellPrice\":82000000}";
        String location = mockMvc.perform(post("/api/gold-prices")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.goldType").value("SJC"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get("/api/gold-prices").param("goldType", "sj")
                        .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goldType\":\"SJC\",\"buyPrice\":81000000,\"sellPrice\":83000000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyPrice").value(81000000));

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidRequestAndPageSize() throws Exception {
        mockMvc.perform(post("/api/gold-prices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goldType\":\"\",\"buyPrice\":100,\"sellPrice\":90}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/gold-prices").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exposesOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Gold Price API"))
                .andExpect(jsonPath("$.paths['/api/gold-prices']").exists());
    }
}
