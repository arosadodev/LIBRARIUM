package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.entity.Libro;
import ovh.emserv.librarium.entity.Resena;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.ResenaRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {
    @Mock
    private ResenaRepository resenaRepository;
    @Mock
    private LibroRepository libroRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private ResenaService service;

    @Test
    void rechazaDosResenasDelMismoUsuarioYLibro() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        when(libroRepository.findById(3L)).thenReturn(Optional.of(new Libro()));
        when(usuarioRepository.findByUsername("usernick")).thenReturn(Optional.of(usuario));
        when(resenaRepository.existsByLibroIdAndUsuarioId(3L, 7L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crearResenaSegura(new ResenaDTO(5, "texto", 3L), "usernick"));
    }
}
