package ovh.emserv.librarium.service;

import ovh.emserv.librarium.entity.Libro;
import ovh.emserv.librarium.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {
    private final LibroRepository repository;

    public LibroService(LibroRepository repository) {
        this.repository = repository;
    }

    public List<Libro> listar() {
        return repository.findAll();
    }

    public Libro obtener(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public List<Libro> buscar(String texto) {
        return repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(texto, texto);
    }
}
