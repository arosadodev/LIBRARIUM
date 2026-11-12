package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.LoginPeticion;
import ovh.emserv.librarium.dto.TokenRespuesta;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.repository.UsuarioRepository;
import ovh.emserv.librarium.service.ServicioJwt;
import ovh.emserv.librarium.service.UsuarioDetallesService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticacionControllerTest {
    @Test
    void iniciarSesionAutenticaUsandoNickYDevuelveRol() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        ServicioJwt servicioJwt = mock(ServicioJwt.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AutenticacionController controller = new AutenticacionController(
                authenticationManager, servicioJwt, usuarioRepository, passwordEncoder);

        var autenticacion = new UsernamePasswordAuthenticationToken(
                "lector", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Usuario usuario = new Usuario();
        usuario.setUsername("lector");
        usuario.setEmail("lector@librarium.local");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(autenticacion);
        when(servicioJwt.generarToken(autenticacion)).thenReturn("jwt-de-prueba");
        when(usuarioRepository.findByUsername("lector")).thenReturn(Optional.of(usuario));

        var response = controller.iniciarSesion(new LoginPeticion(" lector ", "contrasena"));

        var captor = org.mockito.ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("lector", captor.getValue().getPrincipal());
        assertEquals("contrasena", captor.getValue().getCredentials());
        verify(usuarioRepository).findByUsername("lector");
        assertEquals(new TokenRespuesta("jwt-de-prueba", "lector", "ROLE_USER"), response.getBody());
    }

    @Test
    void detallesDeUsuarioBuscaPorNickEspecifico() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario();
        usuario.setUsername("Andres");
        when(usuarioRepository.findByUsername("Andres")).thenReturn(Optional.of(usuario));

        var detalles = new UsuarioDetallesService(usuarioRepository).loadUserByUsername("Andres");

        assertSame(usuario, detalles);
        verify(usuarioRepository).findByUsername("Andres");
    }
}
