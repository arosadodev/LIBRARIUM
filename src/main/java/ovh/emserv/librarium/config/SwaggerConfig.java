package ovh.emserv.librarium.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * @author mit
 * @date 10/11/26 12:02
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Librarium Marginalia API", version = "v1", description = "API REST para el proyecto Librarium"))
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {
    // Para proteger un endpoint en Swagger, simplemente añade la anotación:
    // @SecurityRequirement(name = "BearerAuth")
    // encima del método de tu controlador.
}