package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.OpenLibraryImportarDTO;
import ovh.emserv.librarium.entity.Autor;
import ovh.emserv.librarium.entity.Libro;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;
import ovh.emserv.librarium.repository.AutorRepository;
import ovh.emserv.librarium.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;

    public LibroService(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public Libro importarDesdeOpenLibrary(OpenLibraryImportarDTO dto) {
        // 1. Evitar libros duplicados buscando por su clave de Open Library
        if (libroRepository.findByOpenLibraryKey(dto.key()).isPresent()) {
            throw new ConflictoException("El libro ya existe en el catálogo local");
        }

        // 2. Crear el libro y asociarle sus autores
        Libro nuevoLibro = new Libro();
        nuevoLibro.setTitulo(dto.title());
        nuevoLibro.setOpenLibraryKey(dto.key());
        nuevoLibro.setIsbn(dto.isbn());
        nuevoLibro.setAnoPublicacion(dto.firstPublishYear());
        nuevoLibro.setPortadaUrl(dto.coverUrl());

        if (dto.authors() == null || dto.authors().isEmpty()) {
            nuevoLibro.getAutores().add(obtenerOCrearAutor("Autor desconocido"));
        } else {
            dto.authors().stream()
                    .filter(nombre -> nombre != null && !nombre.isBlank())
                    .distinct()
                    .map(this::obtenerOCrearAutor)
                    .forEach(nuevoLibro.getAutores()::add);
        }

        return libroRepository.save(nuevoLibro);
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro obtener(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + id));
    }

    public List<Libro> buscar(String texto) {
        return libroRepository.buscarPorTituloOAutor(texto);
    }

    private Autor obtenerOCrearAutor(String nombre) {
        return autorRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> autorRepository.save(new Autor(null, nombre)));
    }
}
