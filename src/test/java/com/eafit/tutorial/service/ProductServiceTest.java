package com.eafit.tutorial.service;

import com.eafit.tutorial.exception.ProductAlreadyExistsException;
import com.eafit.tutorial.exception.ProductNotFoundException;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import com.eafit.tutorial.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product(
            "Laptop Gaming",
            "Laptop de alto rendimiento",
            new BigDecimal("2999.99"),
            "Electrónicos",
            15
        );
        testProduct.setId(1L);
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop Gaming");
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getAllProducts_WithPageable_ShouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(Arrays.asList(testProduct));
        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<Product> result = productService.getAllProducts(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository).findByActiveTrue(pageable);
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Laptop Gaming");
        verify(productRepository).findByIdAndActiveTrue(1L);
    }

    @Test
    void getProductById_WhenNotExists_ShouldReturnEmpty() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(99L);

        assertThat(result).isEmpty();
        verify(productRepository).findByIdAndActiveTrue(99L);
    }

    @Test
    void createProduct_WithValidData_ShouldReturnSavedProduct() {
        when(productRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop Gaming");
        verify(productRepository).save(testProduct);
    }

    @Test
    void createProduct_WithDuplicateName_ShouldThrowException() {
        when(productRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(testProduct))
            .isInstanceOf(ProductAlreadyExistsException.class)
            .hasMessageContaining("Ya existe un producto con el nombre");

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WhenExists_ShouldUpdateAndReturn() {
        Product updatedData = new Product(
            "Laptop Gaming Pro",
            "Laptop mejorada",
            new BigDecimal("3499.99"),
            "Electrónicos",
            20
        );

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(1L, updatedData);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenNotExists_ShouldThrowException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, testProduct))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Producto no encontrado");

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_WhenExists_ShouldMarkAsInactive() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.deleteProduct(1L);

        verify(productRepository).save(argThat(product -> !product.getActive()));
    }

    @Test
    void deleteProduct_WhenNotExists_ShouldThrowException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(99L))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryIgnoreCaseAndActiveTrue("Electrónicos"))
            .thenReturn(products);

        List<Product> result = productService.getProductsByCategory("Electrónicos");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electrónicos");
        verify(productRepository).findByCategoryIgnoreCaseAndActiveTrue("Electrónicos");
    }

    @Test
    void getProductsByPriceRange_WithValidRange_ShouldReturnProducts() {
        BigDecimal minPrice = new BigDecimal("1000.00");
        BigDecimal maxPrice = new BigDecimal("5000.00");
        List<Product> products = Arrays.asList(testProduct);
        
        when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(products);

        List<Product> result = productService.getProductsByPriceRange(minPrice, maxPrice);

        assertThat(result).hasSize(1);
        verify(productRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    void getProductsByPriceRange_WithInvalidRange_ShouldThrowException() {
        BigDecimal minPrice = new BigDecimal("5000.00");
        BigDecimal maxPrice = new BigDecimal("1000.00");

        assertThatThrownBy(() -> productService.getProductsByPriceRange(minPrice, maxPrice))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("precio mínimo no puede ser mayor");

        verify(productRepository, never()).findByPriceRange(any(), any());
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Laptop")).thenReturn(products);

        List<Product> result = productService.searchProductsByName("Laptop");

        assertThat(result).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("Laptop");
    }

    @Test
    void getProductsWithLowStock_ShouldReturnLowStockProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByStockLessThanAndActiveTrue(20)).thenReturn(products);

        List<Product> result = productService.getProductsWithLowStock(20);

        assertThat(result).hasSize(1);
        verify(productRepository).findByStockLessThanAndActiveTrue(20);
    }

    @Test
    void updateStock_WhenExists_ShouldUpdateStock() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateStock(1L, 25);

        assertThat(result).isNotNull();
        verify(productRepository).save(argThat(product -> product.getStock() == 25));
    }

    @Test
    void updateStock_WithNegativeValue_ShouldThrowException() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> productService.updateStock(1L, -5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("stock no puede ser negativo");

        verify(productRepository, never()).save(any());
    }

    @Test
    void existsProduct_WhenExists_ShouldReturnTrue() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));

        boolean result = productService.existsProduct(1L);

        assertThat(result).isTrue();
    }

    @Test
    void existsProduct_WhenNotExists_ShouldReturnFalse() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        boolean result = productService.existsProduct(99L);

        assertThat(result).isFalse();
    }
}

