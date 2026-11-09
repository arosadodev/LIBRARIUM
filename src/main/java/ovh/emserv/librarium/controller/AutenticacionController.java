package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.LoginPeticion;
import ovh.emserv.librarium.dto.RegistroPeticion;
import ovh.emserv.librarium.dto.TokenRespuesta;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.repository.UsuarioRepository;
import ovh.emserv.librarium.service.ServicioJwt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    public AutenticacionController(AuthenticationManager gestorAutenticacion, ServicioJwt servicioJwt, UsuarioRepository usuarioRepository, PasswordEncoder codificadorContrasena) {
        this.gestorAutenticacion = gestorAutenticacion;
        this.servicioJwt = servicioJwt;
        this.usuarioRepository = usuarioRepository;
        this.codificadorContrasena = codificadorContrasena;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroPeticion peticion) {
        // Verificar si el email ya existe[cite: 21]
        if (usuarioRepository.findByEmail(peticion.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya está en uso");
        }

        // Crear entidad Usuario[cite: 21]
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(peticion.username());
        nuevoUsuario.setEmail(peticion.email());
        nuevoUsuario.setPasswordHash(codificadorContrasena.encode(peticion.password())); // Cifrado obligatorio[cite: 21]
        nuevoUsuario.setRol(Usuario.Rol.ROLE_USER);

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginPeticion peticion) {
        try {
            // Verificar email y contraseña en la base de datos[cite: 21]
            Authentication credencialesValidadas = gestorAutenticacion.authenticate(new UsernamePasswordAuthenticationToken(peticion.email(), peticion.password()));

            // Generar el JWT[cite: 21]
            String token = servicioJwt.generarToken(credencialesValidadas);

            // Devolver el token al cliente[cite: 21]
            return ResponseEntity.ok(new TokenRespuesta(token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}
