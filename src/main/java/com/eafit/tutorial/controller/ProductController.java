package com.eafit.tutorial.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.dto.PagedResponse;
import com.eafit.tutorial.dto.ProductDTO;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.service.ProductService;
import com.eafit.tutorial.util.ProductMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "API para gestión de productos")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Operation(
        summary = "Obtener productos",
        description = "Obtiene todos los productos activos con paginación opcional y ordenamiento"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de consulta inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<Object>> getAllProducts(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size,

            @Parameter(description = "Campo de ordenamiento", example = "name")
            @RequestParam(value = "sort", defaultValue = "id") String sortField,

            @Parameter(description = "Dirección de ordenamiento", example = "asc")
            @RequestParam(value = "direction", defaultValue = "asc") String sortDirection,

            @Parameter(description = "Si es true, retorna lista simple sin paginación")
            @RequestParam(value = "unpaged", defaultValue = "false") boolean unpaged) {

        logger.debug("GET /api/v1/products - page: {}, size: {}, sort: {}, direction: {}, unpaged: {}",
                    page, size, sortField, sortDirection, unpaged);

        try {
            if (unpaged) {
                List<Product> products = productService.getAllProducts();
                List<ProductDTO> productDTOs = productMapper.toDTOList(products);

                return ResponseEntity.ok(
                    com.eafit.tutorial.dto.ApiResponse.success(productDTOs,
                        "Productos obtenidos exitosamente")
                );
            } else {
                Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

                Page<Product> productPage = productService.getAllProducts(pageable);
                Page<ProductDTO> productDTOPage = productPage.map(productMapper::toDTO);

                PagedResponse<ProductDTO> pagedResponse = PagedResponse.of(productDTOPage);

                return ResponseEntity.ok(
                    com.eafit.tutorial.dto.ApiResponse.success(pagedResponse,
                        "Productos paginados obtenidos exitosamente")
                );
            }
        } catch (Exception e) {
            logger.error("Error al obtener productos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Obtener producto por ID",
        description = "Obtiene un producto específico por su identificador único"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<ProductDTO>> getProductById(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable @Min(1) Long id) {

        logger.debug("GET /api/v1/products/{}", id);

        try {
            Optional<Product> product = productService.getProductById(id);

            if (product.isPresent()) {
                ProductDTO productDTO = productMapper.toDTO(product.get());
                return ResponseEntity.ok(
                    com.eafit.tutorial.dto.ApiResponse.success(productDTO,
                        "Producto encontrado exitosamente")
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.eafit.tutorial.dto.ApiResponse.error(
                        "Producto no encontrado con ID: " + id, 404));
            }
        } catch (Exception e) {
            logger.error("Error al obtener producto con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Crear producto",
        description = "Crea un nuevo producto en el sistema"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El producto ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<ProductDTO>> createProduct(
            @Parameter(description = "Datos del nuevo producto", required = true)
            @Valid @RequestBody CreateProductDTO createProductDTO) {

        logger.debug("POST /api/v1/products - name: {}", createProductDTO.getName());

        try {
            Product product = productMapper.toEntity(createProductDTO);
            Product savedProduct = productService.createProduct(product);
            ProductDTO productDTO = productMapper.toDTO(savedProduct);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.eafit.tutorial.dto.ApiResponse.success(productDTO,
                    "Producto creado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al crear producto", e);

            if (e.getMessage().contains("Ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.eafit.tutorial.dto.ApiResponse.error(e.getMessage(), 409));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza un producto existente por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con datos existentes"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<ProductDTO>> updateProduct(
            @Parameter(description = "ID del producto a actualizar", example = "1", required = true)
            @PathVariable @Min(1) Long id,

            @Parameter(description = "Nuevos datos del producto", required = true)
            @Valid @RequestBody CreateProductDTO updateProductDTO) {

        logger.debug("PUT /api/v1/products/{} - name: {}", id, updateProductDTO.getName());

        try {
            Product product = productMapper.toEntity(updateProductDTO);
            Product updatedProduct = productService.updateProduct(id, product);
            ProductDTO productDTO = productMapper.toDTO(updatedProduct);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(productDTO,
                    "Producto actualizado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al actualizar producto con ID: {}", id, e);

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.eafit.tutorial.dto.ApiResponse.error(e.getMessage(), 404));
            } else if (e.getMessage().contains("Ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.eafit.tutorial.dto.ApiResponse.error(e.getMessage(), 409));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Eliminar producto",
        description = "Elimina lógicamente un producto (lo marca como inactivo)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<Void>> deleteProduct(
            @Parameter(description = "ID del producto a eliminar", example = "1", required = true)
            @PathVariable @Min(1) Long id) {

        logger.debug("DELETE /api/v1/products/{}", id);

        try {
            productService.deleteProduct(id);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(null,
                    "Producto eliminado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al eliminar producto con ID: {}", id, e);

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.eafit.tutorial.dto.ApiResponse.error(e.getMessage(), 404));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Buscar por categoría",
        description = "Obtiene todos los productos de una categoría específica"
    )
    @GetMapping("/category/{category}")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<List<ProductDTO>>> getProductsByCategory(
            @Parameter(description = "Nombre de la categoría", example = "Electrónicos", required = true)
            @PathVariable String category) {

        logger.debug("GET /api/v1/products/category/{}", category);

        try {
            List<Product> products = productService.getProductsByCategory(category);
            List<ProductDTO> productDTOs = productMapper.toDTOList(products);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(productDTOs,
                    "Productos encontrados para la categoría: " + category));

        } catch (Exception e) {
            logger.error("Error al buscar productos por categoría: {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Buscar por rango de precio",
        description = "Obtiene productos dentro de un rango de precios específico"
    )
    @GetMapping("/price-range")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<List<ProductDTO>>> getProductsByPriceRange(
            @Parameter(description = "Precio mínimo", example = "100.00", required = true)
            @RequestParam @Min(0) BigDecimal minPrice,

            @Parameter(description = "Precio máximo", example = "1000.00", required = true)
            @RequestParam @Min(0) BigDecimal maxPrice) {

        logger.debug("GET /api/v1/products/price-range - min: {}, max: {}", minPrice, maxPrice);

        try {
            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
            List<ProductDTO> productDTOs = productMapper.toDTOList(products);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(productDTOs,
                    String.format("Productos encontrados en rango $%.2f - $%.2f", minPrice, maxPrice)));

        } catch (IllegalArgumentException e) {
            logger.warn("Rango de precios inválido - min: {}, max: {}", minPrice, maxPrice);
            return ResponseEntity.badRequest()
                .body(com.eafit.tutorial.dto.ApiResponse.error(e.getMessage(), 400));

        } catch (Exception e) {
            logger.error("Error al buscar productos por rango de precio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Buscar por nombre",
        description = "Busca productos que contengan el texto especificado en su nombre"
    )
    @GetMapping("/search")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<List<ProductDTO>>> searchProductsByName(
            @Parameter(description = "Texto a buscar en el nombre", example = "laptop", required = true)
            @RequestParam String name) {

        logger.debug("GET /api/v1/products/search?name={}", name);

        try {
            List<Product> products = productService.searchProductsByName(name);
            List<ProductDTO> productDTOs = productMapper.toDTOList(products);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(productDTOs,
                    "Productos encontrados para búsqueda: " + name));

        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Productos con stock bajo",
        description = "Obtiene productos cuyo stock sea menor al límite especificado"
    )
    @GetMapping("/low-stock")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<List<ProductDTO>>> getProductsWithLowStock(
            @Parameter(description = "Límite de stock", example = "10", required = true)
            @RequestParam @Min(0) Integer minStock) {

        logger.debug("GET /api/v1/products/low-stock?minStock={}", minStock);

        try {
            List<Product> products = productService.getProductsWithLowStock(minStock);
            List<ProductDTO> productDTOs = productMapper.toDTOList(products);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(productDTOs,
                    "Productos con stock menor a " + minStock));

        } catch (Exception e) {
            logger.error("Error al obtener productos con stock bajo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Actualizar stock",
        description = "Actualiza únicamente el stock de un producto específico"
    )
    @PatchMapping("/{id}/stock")
    public ResponseEntity<com.eafit.tutorial.dto.ApiResponse<ProductDTO>> updateProductStock(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable @Min(1) Long id,

            @Parameter(description = "Nuevo valor de stock", example = "25", required = true)
            @RequestParam @Min(0) Integer stock) {

        logger.debug("PATCH /api/v1/products/{}/stock - newStock: {}", id, stock);

        try {
            Product updatedProduct = productService.updateStock(id, stock);
            ProductDTO productDTO = productMapper.toDTO(updatedProduct);

            return ResponseEntity.ok(
                com.eafit.tutorial.dto.ApiResponse.success(productDTO,
                    "Stock actualizado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al actualizar stock del producto {}", id, e);

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.eafit.tutorial.dto.ApiResponse.error(e.getMessage(), 404));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(com.eafit.tutorial.dto.ApiResponse.error("Error interno del servidor"));
        }
    }
}
