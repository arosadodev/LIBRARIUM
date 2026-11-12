package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.*;
import ovh.emserv.librarium.entity.ForoHilo;
import ovh.emserv.librarium.entity.ForoRespuesta;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.AccesoDenegadoException;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;
import ovh.emserv.librarium.repository.ForoHiloRepository;
import ovh.emserv.librarium.repository.ForoRespuestaRepository;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForoService {
    private final ForoHiloRepository hilos;
    private final ForoRespuestaRepository respuestas;
    private final LibroRepository libros;
    private final UsuarioRepository usuarios;

    public ForoService(ForoHiloRepository hilos, ForoRespuestaRepository respuestas,
                       LibroRepository libros, UsuarioRepository usuarios) {
        this.hilos = hilos;
        this.respuestas = respuestas;
        this.libros = libros;
        this.usuarios = usuarios;
    }

    public List<ForoHiloRespuestaDTO> listar(Long libroId) {
        return (libroId == null ? hilos.findAll() : hilos.findByLibroId(libroId))
                .stream().map(this::convertirHilo).toList();
    }

    public ForoHiloRespuestaDTO crear(ForoHiloDTO dto, String username) {
        ForoHilo hilo = new ForoHilo();
        hilo.setTituloHilo(dto.getTituloHilo());
        hilo.setContenidoInicial(dto.getContenidoInicial());
        hilo.setLibro(libros.findById(dto.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.getLibroId())));
        hilo.setUsuario(obtenerUsuario(username));
        return convertirHilo(hilos.save(hilo));
    }

    public Page<ForoRespuestaSalidaDTO> respuestas(Long hiloId, Pageable pageable) {
        comprobarHilo(hiloId);
        return respuestas.findByHiloId(hiloId, pageable).map(this::convertirRespuesta);
    }

    public ForoRespuestaSalidaDTO crearRespuesta(Long hiloId, ForoRespuestaDTO dto, String username) {
        ForoHilo hilo = comprobarHilo(hiloId);
        ForoRespuesta respuesta = new ForoRespuesta();
        respuesta.setHilo(hilo);
        respuesta.setContenido(dto.contenido());
        respuesta.setUsuario(obtenerUsuario(username));
        return convertirRespuesta(respuestas.save(respuesta));
    }

    public ForoHiloRespuestaDTO actualizarHilo(Long id, ForoHiloDTO dto, String username) {
        ForoHilo hilo = comprobarHilo(id);
        comprobarPermiso(hilo.getUsuario(), username, "hilo");
        hilo.setTituloHilo(dto.getTituloHilo());
        hilo.setContenidoInicial(dto.getContenidoInicial());
        return convertirHilo(hilos.save(hilo));
    }

    public void borrarHilo(Long id, String username) {
        ForoHilo hilo = comprobarHilo(id);
        comprobarPermiso(hilo.getUsuario(), username, "hilo");
        hilos.delete(hilo);
    }

    public ForoRespuestaSalidaDTO actualizarRespuesta(Long id, ForoRespuestaDTO dto, String username) {
        ForoRespuesta respuesta = obtenerRespuesta(id);
        comprobarPermiso(respuesta.getUsuario(), username, "respuesta");
        respuesta.setContenido(dto.contenido());
        return convertirRespuesta(respuestas.save(respuesta));
    }

    public void borrarRespuesta(Long id, String username) {
        ForoRespuesta respuesta = obtenerRespuesta(id);
        comprobarPermiso(respuesta.getUsuario(), username, "respuesta");
        respuestas.delete(respuesta);
    }

    private ForoHilo comprobarHilo(Long id) {
        return hilos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Hilo no encontrado: " + id));
    }

    private ForoRespuesta obtenerRespuesta(Long id) {
        return respuestas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Respuesta no encontrada: " + id));
    }

    private Usuario obtenerUsuario(String username) {
        return usuarios.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
    }

    private void comprobarPermiso(Usuario propietario, String username, String recurso) {
        Usuario solicitante = obtenerUsuario(username);
        if (!propietario.getId().equals(solicitante.getId()) && solicitante.getRol() != Usuario.Rol.ROLE_ADMIN) {
            throw new AccesoDenegadoException("No tienes permisos sobre esta " + recurso);
        }
    }

    private ForoHiloRespuestaDTO convertirHilo(ForoHilo hilo) {
        return new ForoHiloRespuestaDTO(
                hilo.getId(),
                hilo.getTituloHilo(),
                hilo.getContenidoInicial(),
                hilo.getFechaCreacion(),
                hilo.getLibro().getId(),
                new UsuarioResumenDTO(hilo.getUsuario().getId(), hilo.getUsuario().getPublicUsername(), hilo.getUsuario().getDescripcion())
        );
    }

    public ForoHiloRespuestaDTO convertirPublico(ForoHilo hilo) {
        return convertirHilo(hilo);
    }

    private ForoRespuestaSalidaDTO convertirRespuesta(ForoRespuesta respuesta) {
        return new ForoRespuestaSalidaDTO(
                respuesta.getId(),
                respuesta.getContenido(),
                respuesta.getFechaCreacion(),
                respuesta.getHilo().getId(),
                new UsuarioResumenDTO(respuesta.getUsuario().getId(), respuesta.getUsuario().getPublicUsername(), respuesta.getUsuario().getDescripcion())
        );
    }
}
