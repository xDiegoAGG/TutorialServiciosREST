package com.eafit.tutorial.repository;

import com.eafit.tutorial.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct1 = new Product(
            "Laptop Gaming",
            "Laptop de alto rendimiento",
            new BigDecimal("2999.99"),
            "Electrónicos",
            15
        );

        testProduct2 = new Product(
            "Mouse Gamer",
            "Mouse RGB con alta precisión",
            new BigDecimal("79.99"),
            "Perifericos",
            50
        );

        inactiveProduct = new Product(
            "Producto Inactivo",
            "Este producto está inactivo",
            new BigDecimal("100.00"),
            "Miscelanea",
            0
        );
        inactiveProduct.setActive(false);

        productRepository.save(testProduct1);
        productRepository.save(testProduct2);
        productRepository.save(inactiveProduct);
    }

    @Test
    void findByActiveTrue_ShouldReturnOnlyActiveProducts() {
        List<Product> activeProducts = productRepository.findByActiveTrue();

        assertThat(activeProducts).hasSize(2);
        assertThat(activeProducts).extracting(Product::getActive)
            .containsOnly(true);
    }

    @Test
    void findByActiveTrue_WithPageable_ShouldReturnPaginatedActiveProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = productRepository.findByActiveTrue(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Product::getActive)
            .containsOnly(true);
    }

    @Test
    void findByCategoryIgnoreCaseAndActiveTrue_ShouldReturnProductsByCategory() {
        List<Product> electronics = productRepository
            .findByCategoryIgnoreCaseAndActiveTrue("electrónicos");

        assertThat(electronics).hasSize(1);
        assertThat(electronics.get(0).getName()).isEqualTo("Laptop Gaming");
    }

    @Test
    void findByCategoryIgnoreCaseAndActiveTrue_ShouldBeCaseInsensitive() {
        List<Product> electronics = productRepository
            .findByCategoryIgnoreCaseAndActiveTrue("ELECTRÓNICOS");

        assertThat(electronics).hasSize(1);
    }

    @Test
    void findByPriceRange_ShouldReturnProductsInRange() {
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("100.00");

        List<Product> productsInRange = productRepository
            .findByPriceRange(minPrice, maxPrice);

        assertThat(productsInRange).hasSize(1);
        assertThat(productsInRange.get(0).getName()).isEqualTo("Mouse Gamer");
    }

    @Test
    void findByPriceRange_WithWideRange_ShouldReturnAllProducts() {
        BigDecimal minPrice = new BigDecimal("0.01");
        BigDecimal maxPrice = new BigDecimal("10000.00");

        List<Product> productsInRange = productRepository
            .findByPriceRange(minPrice, maxPrice);

        assertThat(productsInRange).hasSize(2);
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        List<Product> products = productRepository
            .findByNameContainingIgnoreCase("gaming");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).contains("Gaming");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldBeCaseInsensitive() {
        List<Product> products = productRepository
            .findByNameContainingIgnoreCase("LAPTOP");

        assertThat(products).hasSize(1);
    }

    @Test
    void findByStockLessThanAndActiveTrue_ShouldReturnLowStockProducts() {
        List<Product> lowStockProducts = productRepository
            .findByStockLessThanAndActiveTrue(20);

        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0).getStock()).isLessThan(20);
    }

    @Test
    void countByCategory_ShouldReturnCorrectCount() {
        Long count = productRepository.countByCategory("Electrónicos");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByCategory_ForNonExistingCategory_ShouldReturnZero() {
        Long count = productRepository.countByCategory("NoExiste");

        assertThat(count).isEqualTo(0);
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_WhenNameExists_ShouldReturnTrue() {
        boolean exists = productRepository
            .existsByNameIgnoreCaseAndIdNot("Laptop Gaming", 999L);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_WhenNameDoesNotExist_ShouldReturnFalse() {
        boolean exists = productRepository
            .existsByNameIgnoreCaseAndIdNot("Producto Inexistente", 999L);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_ShouldExcludeOwnId() {
        Long laptopId = testProduct1.getId();
        boolean exists = productRepository
            .existsByNameIgnoreCaseAndIdNot("Laptop Gaming", laptopId);

        assertThat(exists).isFalse();
    }

    @Test
    void findByIdAndActiveTrue_WhenProductIsActive_ShouldReturnProduct() {
        Optional<Product> product = productRepository
            .findByIdAndActiveTrue(testProduct1.getId());

        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("Laptop Gaming");
    }

    @Test
    void findByIdAndActiveTrue_WhenProductIsInactive_ShouldReturnEmpty() {
        Optional<Product> product = productRepository
            .findByIdAndActiveTrue(inactiveProduct.getId());

        assertThat(product).isEmpty();
    }

    @Test
    void findByIdAndActiveTrue_WhenProductDoesNotExist_ShouldReturnEmpty() {
        Optional<Product> product = productRepository.findByIdAndActiveTrue(999L);

        assertThat(product).isEmpty();
    }

    @Test
    void save_ShouldPersistProduct() {
        Product newProduct = new Product(
            "Teclado Mecánico",
            "Teclado gaming RGB",
            new BigDecimal("149.99"),
            "Perifericos",
            30
        );

        Product saved = productRepository.save(newProduct);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_ShouldUpdateTimestamp() throws InterruptedException {
        Product product = testProduct1;
        LocalDateTime originalUpdatedAt = product.getUpdatedAt();
        
        Thread.sleep(10); // Asegurar que pase algo de tiempo
        
        product.setPrice(new BigDecimal("3500.00"));
        Product updated = productRepository.save(product);

        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    void delete_ShouldRemoveProduct() {
        Long id = testProduct1.getId();
        productRepository.delete(testProduct1);

        Optional<Product> deleted = productRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}

