package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.OpenLibraryImportarDTO;
import ovh.emserv.librarium.entity.Libro;
import ovh.emserv.librarium.repository.AutorRepository;
import ovh.emserv.librarium.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {
    @Mock
    private LibroRepository libroRepository;
    @Mock
    private AutorRepository autorRepository;
    @Mock
    private OpenLibraryService openLibraryService;
    @InjectMocks
    private LibroService service;

    @Test
    void importaLibroConAutoresYEvitaDuplicados() {
        when(libroRepository.findByOpenLibraryKey("/works/OL1")).thenReturn(Optional.empty());
        when(autorRepository.findByNombreIgnoreCase("Autor")).thenReturn(Optional.empty());
        when(autorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(libroRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(openLibraryService.obtenerSinopsis("/works/OL1")).thenReturn("Sinopsis real del libro");

        Libro libro = service.importarDesdeOpenLibrary(new OpenLibraryImportarDTO(
                "/works/OL1", "Título", java.util.List.of("Autor"), "123", 2024, null, null));

        assertEquals("Título", libro.getTitulo());
        assertEquals(1, libro.getAutores().size());
        assertEquals("Sinopsis real del libro", libro.getSinopsis());
    }

    @Test
    void obtenerLibroRecuperaSinopsisSiNoTiene() {
        Libro libroSinSinopsis = new Libro();
        libroSinSinopsis.setId(1L);
        libroSinSinopsis.setTitulo("Título");
        libroSinSinopsis.setOpenLibraryKey("/works/OL1");
        libroSinSinopsis.setSinopsis(null);

        when(libroRepository.findById(1L)).thenReturn(Optional.of(libroSinSinopsis));
        when(openLibraryService.obtenerSinopsis("/works/OL1")).thenReturn("Sinopsis obtenida");
        when(libroRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = service.obtener(1L);

        assertEquals("Sinopsis obtenida", resultado.getSinopsis());
    }
}
