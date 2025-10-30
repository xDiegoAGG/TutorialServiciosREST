package com.eafit.tutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos para actualización parcial de un producto")
public class UpdateProductDTO {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nuevo nombre del producto", example = "Smartphone Pro Max")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Nueva descripción del producto")
    private String description;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio inválido")
    @Schema(description = "Nuevo precio del producto", example = "1299.99")
    private BigDecimal price;

    @Pattern(regexp = "^[A-Za-zÁ-ÿ\\u00f1\\u00d1\\s]+$", message = "La categoría solo puede contener letras y espacios")
    @Schema(description = "Nueva categoría del producto", example = "Tecnología")
    private String category;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Nueva cantidad en stock", example = "75")
    private Integer stock;

    public UpdateProductDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public boolean hasUpdates() {
        return name != null || description != null || price != null ||
               category != null || stock != null;
    }
}
