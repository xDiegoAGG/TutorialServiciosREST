package com.eafit.tutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos requeridos para crear un nuevo producto")
public class CreateProductDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del producto", example = "Smartphone Pro", required = true)
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción del producto", example = "Smartphone de última generación con cámara de 108MP")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio inválido")
    @Schema(description = "Precio del producto", example = "899.99", required = true)
    private BigDecimal price;

    @NotBlank(message = "La categoría es obligatoria")
    @Pattern(regexp = "^[A-Za-zÁ-ÿ\\u00f1\\u00d1\\s]+$", message = "La categoría solo puede contener letras y espacios")
    @Schema(description = "Categoría del producto", example = "Electrónicos", required = true)
    private String category;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad inicial en stock", example = "50", required = true)
    private Integer stock;

    public CreateProductDTO() {}

    public CreateProductDTO(String name, String description, BigDecimal price, String category, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

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
}
