package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.dto.ResenaRespuestaDTO;
import ovh.emserv.librarium.dto.UsuarioResumenDTO;
import ovh.emserv.librarium.entity.Resena;
import ovh.emserv.librarium.exception.AccesoDenegadoException;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.ResenaRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:24
 */
@Service
public class ResenaService {
    private final ResenaRepository resenaRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public ResenaService(ResenaRepository resenaRepository, LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        this.resenaRepository = resenaRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ResenaRespuestaDTO crearResenaSegura(ResenaDTO dto, String username) {
        var libro = libroRepository.findById(dto.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.libroId()));

        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
        if (resenaRepository.existsByLibroIdAndUsuarioId(dto.libroId(), usuario.getId())) {
            throw new ConflictoException("El usuario ya ha reseñado este libro");
        }

        Resena resena = new Resena();
        resena.setPuntuacion(dto.puntuacion());
        resena.setComentario(dto.comentario());
        resena.setLibro(libro);
        resena.setUsuario(usuario);

        return convertir(resenaRepository.save(resena));
    }

    public List<ResenaRespuestaDTO> listar(Long libroId, Long usuarioId) {
        List<Resena> resenas = usuarioId != null ? resenaRepository.findByUsuarioId(usuarioId)
                : libroId != null ? resenaRepository.findByLibroId(libroId) : resenaRepository.findAll();
        return resenas
                .stream().map(this::convertir).toList();
    }

    public ResenaRespuestaDTO actualizar(Long id, ResenaDTO dto, String username) {
        Resena resena = obtener(id);
        comprobarPermiso(resena.getUsuario(), username);
        resena.setPuntuacion(dto.puntuacion());
        resena.setComentario(dto.comentario());
        return convertir(resenaRepository.save(resena));
    }

    public void borrarResena(Long idResena, String username) {
        Resena resena = obtener(idResena);

        var usuarioSolicitante = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

        boolean esElDueno = resena.getUsuario().getId().equals(usuarioSolicitante.getId());
        boolean esAdmin = usuarioSolicitante.getRol().name().equals("ROLE_ADMIN");

        if (!esElDueno && !esAdmin) {
            throw new AccesoDenegadoException("No tienes permisos para borrar esta reseña");
        }

        resenaRepository.delete(resena);
    }

    private Resena obtener(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reseña no existe: " + id));
    }

    private void comprobarPermiso(ovh.emserv.librarium.entity.Usuario propietario, String username) {
        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
        if (!propietario.getId().equals(usuario.getId()) && usuario.getRol() != ovh.emserv.librarium.entity.Usuario.Rol.ROLE_ADMIN) {
            throw new ovh.emserv.librarium.exception.AccesoDenegadoException("No tienes permisos sobre esta reseña");
        }
    }

    private ResenaRespuestaDTO convertir(Resena resena) {
        return new ResenaRespuestaDTO(
                resena.getId(),
                resena.getPuntuacion(),
                resena.getComentario(),
                resena.getFechaRegistro(),
                resena.getLibro().getId(),
                new UsuarioResumenDTO(resena.getUsuario().getId(), resena.getUsuario().getPublicUsername(), resena.getUsuario().getDescripcion())
        );
    }
}
