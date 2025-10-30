package com.eafit.tutorial.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Products API - Tutorial Spring Boot",
        version = "2.0",
        description = "API REST completa para gestión de productos con Spring Boot",
        contact = @Contact(
            name = "Sebastián Gómez",
            email = "sgomez@eafit.edu.co",
            url = "https://eafit.edu.co"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "Servidor de desarrollo",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Servidor de pruebas",
            url = "https://api-test.eafit.edu.co"
        ),
        @Server(
            description = "Servidor de producción",
            url = "https://api.eafit.edu.co"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Ingrese el token JWT en el formato: Bearer {token}"
)
@SecurityScheme(
    name = "API Key",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-API-Key",
    description = "API Key para autenticación de servicios externos"
)
public class OpenApiConfig {
}
