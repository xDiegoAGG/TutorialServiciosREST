package com.eafit.tutorial.controller;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        testProduct = new Product(
            "Laptop Gaming",
            "Laptop de alto rendimiento",
            new BigDecimal("2999.99"),
            "Tecnologia",
            15
        );
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void getAllProducts_ShouldReturnPaginatedProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content", hasSize(1)))
            .andExpect(jsonPath("$.data.content[0].name").value("Laptop Gaming"))
            .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    void getAllProducts_Unpaged_ShouldReturnAllProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("unpaged", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].name").value("Laptop Gaming"));
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
            .andExpect(jsonPath("$.data.name").value("Laptop Gaming"))
            .andExpect(jsonPath("$.data.price").value(2999.99));
    }

    @Test
    void getProductById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreated() throws Exception {
        CreateProductDTO newProduct = new CreateProductDTO(
            "Smartphone Pro",
            "Teléfono de última generación",
            new BigDecimal("899.99"),
            "Electrónicos",
            50
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Smartphone Pro"))
            .andExpect(jsonPath("$.data.price").value(899.99))
            .andExpect(jsonPath("$.data.stock").value(50));
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturn400() throws Exception {
        CreateProductDTO invalidProduct = new CreateProductDTO(
            "",
            "Descripción",
            new BigDecimal("-100"),
            "Electrónicos",
            50
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createProduct_WithDuplicateName_ShouldReturn409() throws Exception {
        CreateProductDTO duplicateProduct = new CreateProductDTO(
            "Laptop Gaming",
            "Otra laptop",
            new BigDecimal("3500.00"),
            "Electrónicos",
            10
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateProduct)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
        CreateProductDTO updatedData = new CreateProductDTO(
            "Laptop Gaming Pro",
            "Laptop mejorada",
            new BigDecimal("3499.99"),
            "Electrónicos",
            20
        );

        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Laptop Gaming Pro"))
            .andExpect(jsonPath("$.data.price").value(3499.99))
            .andExpect(jsonPath("$.data.stock").value(20));
    }

    @Test
    void updateProduct_WhenNotExists_ShouldReturn404() throws Exception {
        CreateProductDTO updatedData = new CreateProductDTO(
            "Producto",
            "Descripción",
            new BigDecimal("100.00"),
            "Categoría",
            10
        );

        mockMvc.perform(put("/api/v1/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteProduct_WhenExists_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(containsString("eliminado")));
    }

    @Test
    void deleteProduct_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/{category}", "Electrónicos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].category").value("Electrónicos"));
    }

    @Test
    void getProductsByPriceRange_ShouldReturnProductsInRange() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "2000.00")
                .param("maxPrice", "3500.00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void getProductsByPriceRange_WithInvalidRange_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "3500.00")
                .param("maxPrice", "2000.00"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "Laptop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].name").value(containsString("Laptop")));
    }

    @Test
    void searchProductsByName_WithNoMatches_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "NoExiste"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getProductsWithLowStock_ShouldReturnLowStockProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/low-stock")
                .param("minStock", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void updateProductStock_WhenExists_ShouldUpdateStock() throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/stock", testProduct.getId())
                .param("stock", "25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.stock").value(25));
    }

    @Test
    void updateProductStock_WithNegativeValue_ShouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/stock", testProduct.getId())
                .param("stock", "-5"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductStock_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/stock", 999L)
                .param("stock", "25"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }
}

