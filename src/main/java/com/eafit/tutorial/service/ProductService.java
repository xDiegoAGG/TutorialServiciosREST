package com.eafit.tutorial.service;

import com.eafit.tutorial.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getAllProducts();

    Page<Product> getAllProducts(Pageable pageable);

    Optional<Product> getProductById(Long id);

    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    List<Product> getProductsByCategory(String category);

    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> searchProductsByName(String name);

    List<Product> getProductsWithLowStock(Integer minStock);

    boolean existsProduct(Long id);

    Product updateStock(Long id, Integer newStock);
}
