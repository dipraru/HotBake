package com.hotbake.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productListPage_returnsOk() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products", "categories"));
    }

    @Test
    void homePage_returnsOk() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("latestProducts"));
    }

    @Test
    void searchPage_withQuery_returnsResults() throws Exception {
        mockMvc.perform(get("/search").param("q", "chocolate"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products", "query"));
    }
}
