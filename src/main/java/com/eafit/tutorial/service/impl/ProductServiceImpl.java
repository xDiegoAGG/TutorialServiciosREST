package com.eafit.tutorial.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eafit.tutorial.exception.ProductAlreadyExistsException;
import com.eafit.tutorial.exception.ProductNotFoundException;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import com.eafit.tutorial.service.ProductService;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Obteniendo todos los productos activos");
        return productRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        logger.debug("Obteniendo productos activos con paginación: {}", pageable);
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        logger.debug("Buscando producto con ID: {}", id);
        return productRepository.findByIdAndActiveTrue(id);
    }

    @Override
    public Product createProduct(Product product) {
        logger.debug("Creando nuevo producto: {}", product.getName());

        if (productRepository.existsByNameIgnoreCaseAndIdNot(product.getName(), 0L)) {
            throw new ProductAlreadyExistsException("Ya existe un producto con el nombre: " + product.getName());
        }

        if (product.getActive() == null) {
            product.setActive(true);
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Producto creado exitosamente con ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        logger.debug("Actualizando producto con ID: {}", id);

        Product existingProduct = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        if (!existingProduct.getName().equalsIgnoreCase(product.getName()) &&
            productRepository.existsByNameIgnoreCaseAndIdNot(product.getName(), id)) {
            throw new ProductAlreadyExistsException("Ya existe otro producto con el nombre: " + product.getName());
        }

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setStock(product.getStock());

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Producto actualizado exitosamente: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        logger.debug("Eliminando producto con ID: {}", id);

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        product.setActive(false);
        productRepository.save(product);

        logger.info("Producto marcado como inactivo: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        logger.debug("Buscando productos por categoría: {}", category);
        return productRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Buscando productos en rango de precio: {} - {}", minPrice, maxPrice);

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }

        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        logger.debug("Buscando productos por nombre: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsWithLowStock(Integer minStock) {
        logger.debug("Buscando productos con stock menor a: {}", minStock);
        return productRepository.findByStockLessThanAndActiveTrue(minStock);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id).isPresent();
    }

    @Override
    public Product updateStock(Long id, Integer newStock) {
        logger.debug("Actualizando stock del producto {}: nuevo stock = {}", id, newStock);

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);

        logger.info("Stock actualizado para producto {}: {}", id, newStock);
        return updatedProduct;
    }
}
