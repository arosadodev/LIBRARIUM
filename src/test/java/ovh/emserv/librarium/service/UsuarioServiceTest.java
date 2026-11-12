package ovh.emserv.librarium.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.emserv.librarium.dto.UsuarioActualizacionDTO;
import ovh.emserv.librarium.dto.UsuarioPerfilDTO;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;
import ovh.emserv.librarium.repository.ForoHiloRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ForoHiloRepository foroHiloRepository;

    @Mock
    private ResenaService resenaService;

    @Mock
    private ForoService foroService;

    @Mock
    private AnotacionService anotacionService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void actualizarPerfilActualExitoso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("usuarioAntiguo");
        usuario.setEmail("test@librarium.local");
        usuario.setDescripcion("Descripcion antigua");

        when(usuarioRepository.findByUsername("usuarioAntiguo")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsername("usuarioNuevo")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(resenaService.listar(null, 1L)).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(1L)).thenReturn(Collections.emptyList());

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO("usuarioNuevo", "Nueva descripcion sobre mi");
        UsuarioPerfilDTO resultado = usuarioService.actualizarPerfilActual("usuarioAntiguo", peticion);

        assertNotNull(resultado);
        assertEquals("usuarioNuevo", resultado.usuario().username());
        assertEquals("Nueva descripcion sobre mi", resultado.descripcion());
        assertEquals("Nueva descripcion sobre mi", resultado.usuario().descripcion());
        assertEquals("test@librarium.local", resultado.usuario().email());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rechazaActualizarAUsuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("miUsuario");
        usuario.setEmail("test@librarium.local");

        when(usuarioRepository.findByUsername("miUsuario")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsername("otroUsuario")).thenReturn(true);

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO("otroUsuario", "Hola");

        assertThrows(ConflictoException.class, () ->
                usuarioService.actualizarPerfilActual("miUsuario", peticion)
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilConMismoNombreNoLanzaConflicto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("miUsuario");
        usuario.setEmail("test@librarium.local");
        usuario.setDescripcion("Descripcion vieja");

        when(usuarioRepository.findByUsername("miUsuario")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(resenaService.listar(null, 1L)).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(1L)).thenReturn(Collections.emptyList());

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO("miUsuario", "Descripcion nueva");
        UsuarioPerfilDTO resultado = usuarioService.actualizarPerfilActual("miUsuario", peticion);

        assertNotNull(resultado);
        assertEquals("miUsuario", resultado.usuario().username());
        assertEquals("Descripcion nueva", resultado.descripcion());
        verify(usuarioRepository, never()).existsByUsername(anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarSoloDescripcionSinEnviarNombre() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("miUsuario");
        usuario.setEmail("test@librarium.local");
        usuario.setDescripcion("Descripcion vieja");

        when(usuarioRepository.findByUsername("miUsuario")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(resenaService.listar(null, 1L)).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(1L)).thenReturn(Collections.emptyList());

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO(null, "Solo cambio de bio");
        UsuarioPerfilDTO resultado = usuarioService.actualizarPerfilActual("miUsuario", peticion);

        assertNotNull(resultado);
        assertEquals("miUsuario", resultado.usuario().username());
        assertEquals("Solo cambio de bio", resultado.descripcion());
        verify(usuarioRepository, never()).existsByUsername(anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void obtenerPerfilLanzaExcepcionSiNoExiste() {
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                usuarioService.obtenerPerfil("inexistente")
        );
    }

    @Test
    void perfilPropioIncluyeCorreoDeContacto() {
        Usuario usuario = usuario("lector", "lector@librarium.local");
        prepararPerfil(usuario);

        UsuarioPerfilDTO resultado = usuarioService.obtenerPerfil("lector", "lector", false);

        assertEquals("lector@librarium.local", resultado.usuario().email());
    }

    @Test
    void perfilPublicoNoIncluyeCorreoDeOtraPersona() {
        Usuario usuario = usuario("lector", "lector@librarium.local");
        prepararPerfil(usuario);

        UsuarioPerfilDTO resultado = usuarioService.obtenerPerfil("lector", "otraPersona", false);

        assertNull(resultado.usuario().email());
    }

    @Test
    void perfilDiferenciaMayusculasYMinusculasEnPropietario() {
        Usuario usuario = usuario("Andres", "andres@librarium.local");
        prepararPerfil(usuario);

        // Si solicita "andres" pero el perfil es "Andres", no es el mismo usuario y no debe ver el correo
        UsuarioPerfilDTO resultado = usuarioService.obtenerPerfil("Andres", "andres", false);

        assertNull(resultado.usuario().email());
    }

    @Test
    void directorioPublicoNoIncluyeCorreos() {
        Usuario usuario = usuario("lector", "lector@librarium.local");
        when(usuarioRepository.findTop50ByOrderByUsernameAsc()).thenReturn(java.util.List.of(usuario));

        var directorio = usuarioService.buscar(null, "otraPersona", false);

        assertNull(directorio.get(0).email());
    }

    @Test
    void administradorPuedeVerCorreoDePerfilYDirectorio() {
        Usuario usuario = usuario("lector", "lector@librarium.local");
        prepararPerfil(usuario);
        when(usuarioRepository.findTop50ByOrderByUsernameAsc()).thenReturn(java.util.List.of(usuario));

        UsuarioPerfilDTO perfil = usuarioService.obtenerPerfil("lector", "admin", true);
        var directorio = usuarioService.buscar(null, "admin", true);

        assertEquals("lector@librarium.local", perfil.usuario().email());
        assertEquals("lector@librarium.local", directorio.get(0).email());
    }

    private Usuario usuario(String username, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setUsername(username);
        usuario.setEmail(email);
        return usuario;
    }

    private void prepararPerfil(Usuario usuario) {
        when(usuarioRepository.findByUsername(usuario.getUsername())).thenReturn(Optional.of(usuario));
        when(resenaService.listar(null, usuario.getId())).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(usuario.getId())).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(usuario.getId())).thenReturn(Collections.emptyList());
    }
}
