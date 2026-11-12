package ovh.emserv.librarium.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${librarium.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,https://librarium.emserv.ovh}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-XSRF-TOKEN")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
