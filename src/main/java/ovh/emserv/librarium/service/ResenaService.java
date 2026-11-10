package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.dto.ResenaRespuestaDTO;
import ovh.emserv.librarium.dto.UsuarioResumenDTO;
import ovh.emserv.librarium.entity.Resena;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.repository.ResenaRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import ovh.emserv.librarium.exception.AccesoDenegadoException;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;

import java.util.List;

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

    public ResenaRespuestaDTO crearResenaSegura(ResenaDTO dto, String emailUsuario) {
        var libro = libroRepository.findById(dto.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado: " + dto.libroId()));

        var usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + emailUsuario));
        if (resenaRepository.existsByLibroIdAndUsuarioId(dto.libroId(), usuario.getId())) {
            throw new ConflictoException("El usuario ya ha reseñado este libro");
        }

        Resena resena = new Resena();
        resena.setPuntuacion(dto.puntuacion());
        resena.setComentario(dto.comentario());
        resena.setLibro(libro);
        resena.setUsuario(usuario); // Asignación 100% segura desde el servidor

        return convertir(resenaRepository.save(resena));
    }

    public List<ResenaRespuestaDTO> listar(Long libroId, Long usuarioId) {
        List<Resena> resenas = usuarioId != null ? resenaRepository.findByUsuarioId(usuarioId)
                : libroId != null ? resenaRepository.findByLibroId(libroId) : resenaRepository.findAll();
        return resenas
                .stream().map(this::convertir).toList();
    }

    public ResenaRespuestaDTO actualizar(Long id, ResenaDTO dto, String email) {
        Resena resena = obtener(id);
        comprobarPermiso(resena.getUsuario(), email);
        resena.setPuntuacion(dto.puntuacion());
        resena.setComentario(dto.comentario());
        return convertir(resenaRepository.save(resena));
    }

    public void borrarResena(Long idResena, String emailUsuarioLogueado) {
        // 1. Obtener la reseña y el usuario que hace la petición
        Resena resena = obtener(idResena);

        var usuarioSolicitante = usuarioRepository.findByEmail(emailUsuarioLogueado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + emailUsuarioLogueado));

        // 2. Comprobar permisos: ¿Es el dueño o es un administrador?
        boolean esElDueno = resena.getUsuario().getId().equals(usuarioSolicitante.getId());
        boolean esAdmin = usuarioSolicitante.getRol().name().equals("ROLE_ADMIN");

        if (!esElDueno && !esAdmin) {
            // Lanza una excepción (Puedes crear un AccesoDenegadoException personalizado si quieres)
            throw new AccesoDenegadoException("No tienes permisos para borrar esta reseña");
        }

        // 3. Si pasa la validación de seguridad, se borra
        resenaRepository.delete(resena);
    }

    private Resena obtener(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reseña no existe: " + id));
    }

    private void comprobarPermiso(ovh.emserv.librarium.entity.Usuario propietario, String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));
        if (!propietario.getId().equals(usuario.getId()) && usuario.getRol() != ovh.emserv.librarium.entity.Usuario.Rol.ROLE_ADMIN) {
            throw new ovh.emserv.librarium.exception.AccesoDenegadoException("No tienes permisos sobre esta reseña");
        }
    }

    private ResenaRespuestaDTO convertir(Resena resena) {
        return new ResenaRespuestaDTO(resena.getId(), resena.getPuntuacion(), resena.getComentario(),
                resena.getFechaRegistro(), resena.getLibro().getId(),
                new UsuarioResumenDTO(resena.getUsuario().getId(), resena.getUsuario().getUsername()));
    }
}