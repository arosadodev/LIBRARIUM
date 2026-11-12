package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.LoginPeticion;
import ovh.emserv.librarium.dto.RegistroPeticion;
import ovh.emserv.librarium.dto.TokenRespuesta;
import ovh.emserv.librarium.dto.CsrfRespuesta;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.exception.CredencialesInvalidasException;
import ovh.emserv.librarium.repository.UsuarioRepository;
import ovh.emserv.librarium.service.ServicioJwt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

/**
 * @author mit
 * @date 9/11/26 19:55
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    private final AuthenticationManager gestorAutenticacion;
    private final ServicioJwt servicioJwt;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorContrasena;
    private final boolean cookieSecure;

    private String nombreCookieJwt() {
        return cookieSecure ? "__Host-LIBRARIUM_AUTH" : "LIBRARIUM_AUTH";
    }

    @Autowired
    public AutenticacionController(AuthenticationManager gestorAutenticacion, ServicioJwt servicioJwt,
                                   UsuarioRepository usuarioRepository, PasswordEncoder codificadorContrasena,
                                   @Value("${librarium.auth.cookie-secure:false}") boolean cookieSecure) {
        this.gestorAutenticacion = gestorAutenticacion;
        this.servicioJwt = servicioJwt;
        this.usuarioRepository = usuarioRepository;
        this.codificadorContrasena = codificadorContrasena;
        this.cookieSecure = cookieSecure;
    }

    public AutenticacionController(AuthenticationManager gestorAutenticacion, ServicioJwt servicioJwt,
                                   UsuarioRepository usuarioRepository, PasswordEncoder codificadorContrasena) {
        this(gestorAutenticacion, servicioJwt, usuarioRepository, codificadorContrasena, false);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroPeticion peticion) {
        String username = peticion.username().trim();
        String email = peticion.email().trim().toLowerCase(java.util.Locale.ROOT);
        // Verificar si el email ya existe
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ConflictoException("El email ya está en uso");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictoException("El nombre de usuario ya está en uso");
        }

        // Crear entidad Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPasswordHash(codificadorContrasena.encode(peticion.password())); // Cifrado obligatorio
        nuevoUsuario.setRol(Usuario.Rol.ROLE_USER);

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginPeticion peticion) {
        try {
            String username = peticion.username().trim();
            Authentication credencialesValidadas = gestorAutenticacion.authenticate(new UsernamePasswordAuthenticationToken(username, peticion.password()));

            // Generar el JWT
            String token = servicioJwt.generarToken(credencialesValidadas);

            // El navegador guarda el JWT en una cookie HttpOnly; JavaScript no puede leerlo.
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new CredencialesInvalidasException());
            ResponseCookie cookie = ResponseCookie.from(nombreCookieJwt(), token)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(Duration.ofSeconds(servicioJwt.getTiempoExpiracionSegundos()))
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(new TokenRespuesta(usuario.getPublicUsername(), usuario.getRol().name()));

        } catch (AuthenticationException exception) {
            throw new CredencialesInvalidasException();
        }
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfRespuesta> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new CsrfRespuesta(csrfToken.getToken(), csrfToken.getHeaderName()));
    }

    @GetMapping("/session")
    public TokenRespuesta session(Authentication autenticacion) {
        String rol = autenticacion.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN"))
                .findFirst()
                .orElse("ROLE_USER");
        return new TokenRespuesta(autenticacion.getName(), rol);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion() {
        ResponseCookie cookie = ResponseCookie.from(nombreCookieJwt(), "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
