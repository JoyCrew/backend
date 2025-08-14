package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.PagedProductResponse;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProductService productService;
  @MockBean
  private JwtUtil jwtUtil;
  @MockBean
  private EmployeeDetailsService employeeDetailsService;

  @Test
  @DisplayName("GET /api/products - Should return all products")
  void getAllProducts_Success() throws Exception {
    // Given
    PagedProductResponse mockResponse = new PagedProductResponse(
        Collections.emptyList(), 0, 20, 0, 0, false, false
    );
    when(productService.getAllProducts(anyInt(), anyInt())).thenReturn(mockResponse);

    // When & Then
    mockMvc.perform(get("/api/products")
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("GET /api/products/search - Should return products matching the keyword")
  void searchProductsByName_Success() throws Exception {
    // Given
    PagedProductResponse mockResponse = new PagedProductResponse(
        Collections.emptyList(), 0, 20, 0, 0, false, false
    );
    when(productService.searchProductsByName(anyString(), anyInt(), anyInt()))
        .thenReturn(mockResponse);

    // When & Then
    mockMvc.perform(get("/api/products/search")
            .param("keyword", "Test")
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }
}