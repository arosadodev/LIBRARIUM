package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.AnotacionDTO;
import ovh.emserv.librarium.entity.Anotacion;
import ovh.emserv.librarium.repository.AnotacionRepository;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;

import java.util.List;

@Service
public class AnotacionService {
    private final AnotacionRepository repository;
    private final LibroRepository libros;
    private final UsuarioRepository usuarios;

    public AnotacionService(AnotacionRepository repository, LibroRepository libros, UsuarioRepository usuarios) {
        this.repository = repository;
        this.libros = libros;
        this.usuarios = usuarios;
    }

    public List<Anotacion> listar(Long libroId) {
        return libroId == null ? repository.findAll() : repository.findByLibroId(libroId);
    }

    public Anotacion crear(AnotacionDTO dto) {
        Anotacion anotacion = new Anotacion();
        anotacion.setCapituloNumero(dto.getCapituloNumero());
        anotacion.setPagina(dto.getPagina());
        anotacion.setTipo(dto.getTipo());
        anotacion.setContenido(dto.getContenido());
        anotacion.setLibro(libros.findById(dto.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.getLibroId())));
        anotacion.setUsuario(usuarios.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + dto.getUsuarioId())));
        return repository.save(anotacion);
    }
}
