package ovh.emserv.librarium.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import ovh.emserv.librarium.dto.UsuarioActualizacionDTO;
import ovh.emserv.librarium.dto.UsuarioPerfilDTO;
import ovh.emserv.librarium.dto.UsuarioResumenDTO;
import ovh.emserv.librarium.service.UsuarioService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void listarUsuariosDevuelveLista() {
        UsuarioResumenDTO resumen = new UsuarioResumenDTO(1L, "lector1", "Bio");
        when(usuarioService.buscar("lec")).thenReturn(List.of(resumen));

        List<UsuarioResumenDTO> resultado = usuarioController.listar("lec");
        assertEquals(1, resultado.size());
        assertEquals("lector1", resultado.get(0).username());
    }

    @Test
    void obtenerPerfilPublicoDevuelvePerfil() {
        UsuarioResumenDTO resumen = new UsuarioResumenDTO(1L, "lector1", "Lector empedernido");
        UsuarioPerfilDTO perfil = new UsuarioPerfilDTO(
                resumen,
                "Lector empedernido",
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        when(usuarioService.obtenerPerfil("lector1")).thenReturn(perfil);

        UsuarioPerfilDTO resultado = usuarioController.perfil("lector1");
        assertNotNull(resultado);
        assertEquals("lector1", resultado.usuario().username());
        assertEquals("Lector empedernido", resultado.descripcion());
    }

    @Test
    void miPerfilConAutenticacionDevuelvePerfil() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@librarium.local");

        UsuarioResumenDTO resumen = new UsuarioResumenDTO(1L, "miUsuario", "Mi bio");
        UsuarioPerfilDTO perfil = new UsuarioPerfilDTO(
                resumen,
                "Mi bio",
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        when(usuarioService.obtenerPerfilActual("test@librarium.local")).thenReturn(perfil);

        UsuarioPerfilDTO resultado = usuarioController.miPerfil(auth);
        assertNotNull(resultado);
        assertEquals("miUsuario", resultado.usuario().username());
        assertEquals("Mi bio", resultado.descripcion());
    }

    @Test
    void actualizarMiPerfilLlamaAServicio() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@librarium.local");

        UsuarioActualizacionDTO dto = new UsuarioActualizacionDTO("nuevoNombre", "Nueva bio");
        UsuarioResumenDTO resumen = new UsuarioResumenDTO(1L, "nuevoNombre", "Nueva bio");
        UsuarioPerfilDTO perfil = new UsuarioPerfilDTO(
                resumen,
                "Nueva bio",
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(usuarioService.actualizarPerfilActual("test@librarium.local", dto)).thenReturn(perfil);

        UsuarioPerfilDTO resultado = usuarioController.actualizarMiPerfil(auth, dto);
        assertNotNull(resultado);
        assertEquals("nuevoNombre", resultado.usuario().username());
        assertEquals("Nueva bio", resultado.descripcion());
        verify(usuarioService).actualizarPerfilActual("test@librarium.local", dto);
    }
}
