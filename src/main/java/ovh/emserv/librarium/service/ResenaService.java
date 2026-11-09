package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.entity.Resena;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.ResenaRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;

import java.util.List;

@Service
public class ResenaService {
    private final ResenaRepository repository;
    private final LibroRepository libros;
    private final UsuarioRepository usuarios;

    public ResenaService(ResenaRepository repository, LibroRepository libros, UsuarioRepository usuarios) {
        this.repository = repository;
        this.libros = libros;
        this.usuarios = usuarios;
    }

    public List<Resena> listar(Long libroId) {
        return libroId == null ? repository.findAll() : repository.findByLibroId(libroId);
    }

    public Resena crear(ResenaDTO dto) {
        Resena resena = new Resena();
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setComentario(dto.getComentario());
        resena.setLibro(libros.findById(dto.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.getLibroId())));
        resena.setUsuario(usuarios.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + dto.getUsuarioId())));
        return repository.save(resena);
    }
}
