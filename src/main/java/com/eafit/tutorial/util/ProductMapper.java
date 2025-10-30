package com.eafit.tutorial.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.dto.ProductDTO;
import com.eafit.tutorial.dto.UpdateProductDTO;
import com.eafit.tutorial.model.Product;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStock(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Product toEntity(CreateProductDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        Product product = new Product();
        product.setName(createDTO.getName());
        product.setDescription(createDTO.getDescription());
        product.setPrice(createDTO.getPrice());
        product.setCategory(createDTO.getCategory());
        product.setStock(createDTO.getStock());
        product.setActive(true);

        return product;
    }

    public void updateEntity(Product product, UpdateProductDTO updateDTO) {
        if (product == null || updateDTO == null) {
            return;
        }

        if (updateDTO.getName() != null) {
            product.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getCategory() != null) {
            product.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }
    }
}
