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

        when(usuarioRepository.findByEmail("test@librarium.local")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCase("usuarioNuevo")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioRepository.findByUsernameIgnoreCase("usuarioNuevo")).thenReturn(Optional.of(usuario));
        when(resenaService.listar(null, 1L)).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(1L)).thenReturn(Collections.emptyList());

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO("usuarioNuevo", "Nueva descripcion sobre mi");
        UsuarioPerfilDTO resultado = usuarioService.actualizarPerfilActual("test@librarium.local", peticion);

        assertNotNull(resultado);
        assertEquals("usuarioNuevo", resultado.usuario().username());
        assertEquals("Nueva descripcion sobre mi", resultado.descripcion());
        assertEquals("Nueva descripcion sobre mi", resultado.usuario().descripcion());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rechazaActualizarAUsuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("miUsuario");
        usuario.setEmail("test@librarium.local");

        when(usuarioRepository.findByEmail("test@librarium.local")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCase("otroUsuario")).thenReturn(true);

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO("otroUsuario", "Hola");

        assertThrows(ConflictoException.class, () ->
                usuarioService.actualizarPerfilActual("test@librarium.local", peticion)
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

        when(usuarioRepository.findByEmail("test@librarium.local")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioRepository.findByUsernameIgnoreCase("miUsuario")).thenReturn(Optional.of(usuario));
        when(resenaService.listar(null, 1L)).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(1L)).thenReturn(Collections.emptyList());

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO("miUsuario", "Descripcion nueva");
        UsuarioPerfilDTO resultado = usuarioService.actualizarPerfilActual("test@librarium.local", peticion);

        assertNotNull(resultado);
        assertEquals("miUsuario", resultado.usuario().username());
        assertEquals("Descripcion nueva", resultado.descripcion());
        verify(usuarioRepository, never()).existsByUsernameIgnoreCase(anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarSoloDescripcionSinEnviarNombre() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("miUsuario");
        usuario.setEmail("test@librarium.local");
        usuario.setDescripcion("Descripcion vieja");

        when(usuarioRepository.findByEmail("test@librarium.local")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioRepository.findByUsernameIgnoreCase("miUsuario")).thenReturn(Optional.of(usuario));
        when(resenaService.listar(null, 1L)).thenReturn(Collections.emptyList());
        when(foroHiloRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(anotacionService.listarPublicas(1L)).thenReturn(Collections.emptyList());

        UsuarioActualizacionDTO peticion = new UsuarioActualizacionDTO(null, "Solo cambio de bio");
        UsuarioPerfilDTO resultado = usuarioService.actualizarPerfilActual("test@librarium.local", peticion);

        assertNotNull(resultado);
        assertEquals("miUsuario", resultado.usuario().username());
        assertEquals("Solo cambio de bio", resultado.descripcion());
        verify(usuarioRepository, never()).existsByUsernameIgnoreCase(anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void obtenerPerfilLanzaExcepcionSiNoExiste() {
        when(usuarioRepository.findByUsernameIgnoreCase("inexistente")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                usuarioService.obtenerPerfil("inexistente")
        );
    }
}
