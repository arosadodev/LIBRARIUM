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
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.time.Instant;

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
    private final boolean cookieSecure;

    public ConfiguracionSeguridad(ServicioJwt servicioJwt, UsuarioDetallesService usuarioDetallesService,
                                  @org.springframework.beans.factory.annotation.Value("${librarium.auth.cookie-secure:false}") boolean cookieSecure) {
        this.servicioJwt = servicioJwt;
        this.usuarioDetallesService = usuarioDetallesService;
        this.cookieSecure = cookieSecure;
    }

    @Bean
    public SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .spa()
                        .csrfTokenRepository(repositorioCsrf()))
                .cors(Customizer.withDefaults())

                // RUTAS PERMITIDAS Y RESTRINGIDAS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/csrf", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/session").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/libros/**").permitAll()// Catálogo de libros público
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/error" // 👇 AÑADIMOS LA RUTA DE ERRORES DE SPRING
                        ).permitAll()
                        .anyRequest().authenticated() // Todo lo demás protegido
                )

                // MODO STATELESS (Sin estado)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // VALIDACIÓN AUTOMÁTICA DE TOKENS JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(resolvedorToken())
                        .jwt(jwt -> jwt
                                .decoder(decodificadorJwt())
                                .jwtAuthenticationConverter(conversorDeRolesJwt())
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Autenticación requerida", request.getRequestURI()))
                        .accessDeniedHandler((request, response, exception) ->
                                escribirError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "No tienes permisos para acceder a este recurso", request.getRequestURI())));

        return http.build();
    }

    private CookieCsrfTokenRepository repositorioCsrf() {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookieCustomizer(cookie -> cookie
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/"));
        return repository;
    }

    private BearerTokenResolver resolvedorToken() {
        DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();
        return request -> {
            String headerToken = headerResolver.resolve(request);
            if (headerToken != null) return headerToken;
            Cookie[] cookies = request.getCookies();
            if (cookies == null) return null;
            for (Cookie cookie : cookies) {
                if (("__Host-LIBRARIUM_AUTH".equals(cookie.getName()) || "LIBRARIUM_AUTH".equals(cookie.getName()))
                        && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
            return null;
        };
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

    private void escribirError(HttpServletResponse response, int status, String mensaje, String ruta)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
                """.formatted(Instant.now(), status, status == 401 ? "Unauthorized" : "Forbidden", mensaje, ruta));
    }
}
