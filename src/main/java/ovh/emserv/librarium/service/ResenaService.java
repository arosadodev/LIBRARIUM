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
    private final ResenaRepository resenaRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public ResenaService(ResenaRepository resenaRepository, LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        this.resenaRepository = resenaRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Resena crearResenaSegura(ResenaDTO dto, String emailUsuario) {
        var libro = libroRepository.findById(dto.libroId())
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));

        var usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Resena resena = new Resena();
        resena.setPuntuacion(dto.puntuacion());
        resena.setComentario(dto.comentario());
        resena.setLibro(libro);
        resena.setUsuario(usuario); // Asignación 100% segura desde el servidor

        return resenaRepository.save(resena);
    }

    public void borrarResena(Long idResena, String emailUsuarioLogueado) {
        // 1. Obtener la reseña y el usuario que hace la petición
        Resena resena = resenaRepository.findById(idResena)
                .orElseThrow(() -> new IllegalArgumentException("La reseña no existe"));

        var usuarioSolicitante = usuarioRepository.findByEmail(emailUsuarioLogueado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario inválido"));

        // 2. Comprobar permisos: ¿Es el dueño o es un administrador?
        boolean esElDueno = resena.getUsuario().getId().equals(usuarioSolicitante.getId());
        boolean esAdmin = usuarioSolicitante.getRol().name().equals("ROLE_ADMIN");

        if (!esElDueno && !esAdmin) {
            // Lanza una excepción (Puedes crear un AccesoDenegadoException personalizado si quieres)
            throw new SecurityException("No tienes permisos para borrar esta reseña");
        }

        // 3. Si pasa la validación de seguridad, se borra
        resenaRepository.delete(resena);
    }
}