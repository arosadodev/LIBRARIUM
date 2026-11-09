package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.ForoHiloDTO;
import ovh.emserv.librarium.entity.ForoHilo;
import ovh.emserv.librarium.entity.ForoRespuesta;
import ovh.emserv.librarium.repository.ForoHiloRepository;
import ovh.emserv.librarium.repository.ForoRespuestaRepository;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;

import java.util.List;

@Service
public class ForoService {
    private final ForoHiloRepository hilos;
    private final ForoRespuestaRepository respuestas;
    private final LibroRepository libros;
    private final UsuarioRepository usuarios;

    public ForoService(ForoHiloRepository hilos, ForoRespuestaRepository respuestas, LibroRepository libros, UsuarioRepository usuarios) {
        this.hilos = hilos;
        this.respuestas = respuestas;
        this.libros = libros;
        this.usuarios = usuarios;
    }

    public List<ForoHilo> listar(Long libroId) {
        return libroId == null ? hilos.findAll() : hilos.findByLibroId(libroId);
    }

    public ForoHilo crear(ForoHiloDTO dto) {
        ForoHilo hilo = new ForoHilo();
        hilo.setTituloHilo(dto.getTituloHilo());
        hilo.setContenidoInicial(dto.getContenidoInicial());
        hilo.setLibro(libros.findById(dto.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.getLibroId())));
        hilo.setUsuario(usuarios.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + dto.getUsuarioId())));
        return hilos.save(hilo);
    }

    public List<ForoRespuesta> respuestas(Long hiloId) {
        return respuestas.findByHiloId(hiloId);
    }
}
