package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.AnotacionDTO;
import ovh.emserv.librarium.dto.AnotacionRespuestaDTO;
import ovh.emserv.librarium.dto.UsuarioResumenDTO;
import ovh.emserv.librarium.entity.Anotacion;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.AccesoDenegadoException;
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

    public List<AnotacionRespuestaDTO> listar(Long libroId, Long usuarioId) {
        List<Anotacion> anotaciones = usuarioId != null ? repository.findByUsuarioId(usuarioId)
                : libroId != null ? repository.findByLibroId(libroId) : repository.findAll();
        return anotaciones
                .stream().map(this::convertir).toList();
    }

    public List<AnotacionRespuestaDTO> listarPublicas(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::convertir).toList();
    }

    public AnotacionRespuestaDTO crear(AnotacionDTO dto, String email) {
        Anotacion anotacion = new Anotacion();
        anotacion.setCapituloNumero(dto.getCapituloNumero());
        anotacion.setPagina(dto.getPagina());
        anotacion.setTipo(dto.getTipo());
        anotacion.setContenido(dto.getContenido());
        anotacion.setLibro(libros.findById(dto.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.getLibroId())));
        anotacion.setUsuario(obtenerUsuario(email));
        return convertir(repository.save(anotacion));
    }

    public AnotacionRespuestaDTO actualizar(Long id, AnotacionDTO dto, String email) {
        Anotacion anotacion = obtener(id);
        comprobarPermiso(anotacion.getUsuario(), email);
        anotacion.setCapituloNumero(dto.getCapituloNumero());
        anotacion.setPagina(dto.getPagina());
        anotacion.setTipo(dto.getTipo());
        anotacion.setContenido(dto.getContenido());
        return convertir(repository.save(anotacion));
    }

    public void borrar(Long id, String email) {
        Anotacion anotacion = obtener(id);
        comprobarPermiso(anotacion.getUsuario(), email);
        repository.delete(anotacion);
    }

    private Anotacion obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Anotación no encontrada: " + id));
    }

    private Usuario obtenerUsuario(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));
    }

    private void comprobarPermiso(Usuario propietario, String email) {
        Usuario solicitante = obtenerUsuario(email);
        if (!propietario.getId().equals(solicitante.getId()) && solicitante.getRol() != Usuario.Rol.ROLE_ADMIN) {
            throw new AccesoDenegadoException("No tienes permisos sobre esta anotación");
        }
    }

    private AnotacionRespuestaDTO convertir(Anotacion anotacion) {
        return new AnotacionRespuestaDTO(anotacion.getId(), anotacion.getCapituloNumero(), anotacion.getPagina(),
                anotacion.getTipo(), anotacion.getContenido(), anotacion.getFechaCreacion(), anotacion.getLibro().getId(),
                new UsuarioResumenDTO(anotacion.getUsuario().getId(), anotacion.getUsuario().getPublicUsername(), anotacion.getUsuario().getDescripcion()));
    }
}
