package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.LoginPeticion;
import ovh.emserv.librarium.dto.RegistroPeticion;
import ovh.emserv.librarium.dto.TokenRespuesta;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.repository.UsuarioRepository;
import ovh.emserv.librarium.service.ServicioJwt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
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
        // Verificar si el email ya existe
        if (usuarioRepository.findByEmail(peticion.email()).isPresent()) {
            throw new ConflictoException("El email ya está en uso");
        }

        // Crear entidad Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(peticion.username());
        nuevoUsuario.setEmail(peticion.email());
        nuevoUsuario.setPasswordHash(codificadorContrasena.encode(peticion.password())); // Cifrado obligatorio
        nuevoUsuario.setRol(Usuario.Rol.ROLE_USER);

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginPeticion peticion) {
        try {
            // Verificar email y contraseña en la base de datos
            Authentication credencialesValidadas = gestorAutenticacion.authenticate(new UsernamePasswordAuthenticationToken(peticion.email(), peticion.password()));

            // Generar el JWT
            String token = servicioJwt.generarToken(credencialesValidadas);

            // Devolver el token al cliente
            return ResponseEntity.ok(new TokenRespuesta(token));

        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}
