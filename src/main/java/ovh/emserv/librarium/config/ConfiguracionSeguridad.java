package ovh.emserv.librarium.config;

import ovh.emserv.librarium.service.ServicioJwt;
import ovh.emserv.librarium.service.UsuarioDetallesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author mit
 * @date 9/11/26 20:16
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {
    private final ServicioJwt servicioJwt;
    private final UsuarioDetallesService usuarioDetallesService;

    public ConfiguracionSeguridad(ServicioJwt servicioJwt, UsuarioDetallesService usuarioDetallesService) {
        this.servicioJwt = servicioJwt;
        this.usuarioDetallesService = usuarioDetallesService;
    }

    @Bean
    public SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactivar CSRF para APIs REST[cite: 18]

                // RUTAS PERMITIDAS Y RESTRINGIDAS[cite: 18]
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Login y registro públicos[cite: 18]
                        .requestMatchers(HttpMethod.GET, "/api/libros/**").permitAll() // Catálogo de libros público[cite: 18]
                        .anyRequest().authenticated() // Todo lo demás protegido[cite: 18]
                )

                // MODO STATELESS (Sin estado)[cite: 18]
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // VALIDACIÓN AUTOMÁTICA DE TOKENS JWT[cite: 18]
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(decodificadorJwt())
                                .jwtAuthenticationConverter(conversorDeRolesJwt())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder decodificadorJwt() {
        // Le damos la clave secreta a Spring para que valide las firmas automáticamente[cite: 18]
        return NimbusJwtDecoder.withSecretKey(servicioJwt.getClaveSecreta()).build();
    }

    @Bean
    public JwtAuthenticationConverter conversorDeRolesJwt() {
        // Indica a Spring que los roles están guardados en el campo "roles" del token[cite: 18]
        JwtGrantedAuthoritiesConverter conversor = new JwtGrantedAuthoritiesConverter();
        conversor.setAuthoritiesClaimName("roles");
        conversor.setAuthorityPrefix(""); // Ya incluimos el prefijo ROLE_ al crear el usuario[cite: 18]

        JwtAuthenticationConverter conversorJwt = new JwtAuthenticationConverter();
        conversorJwt.setJwtGrantedAuthoritiesConverter(conversor);
        return conversorJwt;
    }

    @Bean
    public PasswordEncoder codificadorContrasena() {
        return new BCryptPasswordEncoder(); // Hashing seguro para la base de datos[cite: 18]
    }

    @Bean
    public AuthenticationProvider proveedorAutenticacion() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(usuarioDetallesService);
        authProvider.setPasswordEncoder(codificadorContrasena());
        return authProvider;
    }

    @Bean
    public AuthenticationManager gestorAutenticacion(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
