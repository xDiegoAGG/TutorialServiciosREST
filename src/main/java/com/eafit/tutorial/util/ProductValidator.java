package com.eafit.tutorial.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.exception.ValidationException;

@Component
public class ProductValidator {

    private static final String[] FORBIDDEN_WORDS = {"test", "prueba", "demo", "temporal"};
    private static final BigDecimal MAX_PRICE = new BigDecimal("100000.00");
    private static final int MAX_STOCK = 10000;

    public void validateForCreation(CreateProductDTO productDTO) {
        Map<String, String> errors = new HashMap<>();

        if (containsForbiddenWords(productDTO.getName())) {
            errors.put("name", "El nombre del producto no puede contener palabras prohibidas como 'test', 'demo', etc.");
        }

        if (productDTO.getPrice().compareTo(MAX_PRICE) > 0) {
            errors.put("price", "El precio no puede exceder $" + MAX_PRICE);
        }

        if (productDTO.getStock() > MAX_STOCK) {
            errors.put("stock", "El stock no puede exceder " + MAX_STOCK + " unidades");
        }

        validatePriceCategoryCoherence(productDTO.getPrice(), productDTO.getCategory(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("Error de validación de reglas de negocio", errors);
        }
    }

    private boolean containsForbiddenWords(String name) {
        if (name == null) return false;

        String lowerName = name.toLowerCase();
        for (String forbidden : FORBIDDEN_WORDS) {
            if (lowerName.contains(forbidden)) {
                return true;
            }
        }
        return false;
    }

    private void validatePriceCategoryCoherence(BigDecimal price, String category, Map<String, String> errors) {
        if (price == null || category == null) return;

        String lowerCategory = category.toLowerCase();

        if (lowerCategory.contains("electrón") && price.compareTo(new BigDecimal("50.00")) < 0) {
            errors.put("price", "Los productos electrónicos deben tener un precio mínimo de $50.00");
        }

        if (lowerCategory.contains("libro") && price.compareTo(new BigDecimal("200.00")) > 0) {
            errors.put("price", "Los libros no pueden exceder $200.00");
        }

        if (lowerCategory.contains("ropa") || lowerCategory.contains("vestimenta")) {
            if (price.compareTo(new BigDecimal("10.00")) < 0 || price.compareTo(new BigDecimal("1000.00")) > 0) {
                errors.put("price", "La ropa debe tener un precio entre $10.00 y $1,000.00");
            }
        }
    }

    public void validateStockForCategory(String category, Integer stock) {
        if (category == null || stock == null) return;

        String lowerCategory = category.toLowerCase();

        if ((lowerCategory.contains("digital") || lowerCategory.contains("software")) && stock < 1000) {
            throw new ValidationException("Los productos digitales deberían tener stock alto (mínimo 1000)");
        }

        if ((lowerCategory.contains("comida") || lowerCategory.contains("alimento")) && stock > 100) {
            throw new ValidationException("Los productos perecederos no deberían tener stock mayor a 100");
        }
    }
}
