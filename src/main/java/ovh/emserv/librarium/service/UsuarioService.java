package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.UsuarioActualizacionDTO;
import ovh.emserv.librarium.dto.UsuarioPerfilDTO;
import ovh.emserv.librarium.dto.UsuarioResumenDTO;
import ovh.emserv.librarium.entity.Usuario;
import ovh.emserv.librarium.exception.ConflictoException;
import ovh.emserv.librarium.exception.PeticionInvalidaException;
import ovh.emserv.librarium.exception.RecursoNoEncontradoException;
import ovh.emserv.librarium.repository.ForoHiloRepository;
import ovh.emserv.librarium.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final ForoHiloRepository hilos;
    private final ResenaService resenaService;
    private final ForoService foroService;
    private final AnotacionService anotacionService;

    public UsuarioService(UsuarioRepository usuarios, ForoHiloRepository hilos,
                          ResenaService resenaService, ForoService foroService,
                          AnotacionService anotacionService) {
        this.usuarios = usuarios;
        this.hilos = hilos;
        this.resenaService = resenaService;
        this.foroService = foroService;
        this.anotacionService = anotacionService;
    }

    public List<UsuarioResumenDTO> buscar(String query) {
        String normalizada = query == null ? "" : query.trim();
        List<Usuario> encontrados = normalizada.length() < 2
                ? usuarios.findTop50ByOrderByUsernameAsc()
                : usuarios.findTop50ByUsernameContainingIgnoreCaseOrderByUsernameAsc(normalizada);
        return encontrados.stream().map(this::resumen).toList();
    }

    public UsuarioPerfilDTO obtenerPerfil(String username) {
        String nick = username == null ? "" : username.trim();
        Usuario usuario = usuarios.findByUsernameIgnoreCase(nick)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
        Long id = usuario.getId();
        return new UsuarioPerfilDTO(
                resumen(usuario),
                usuario.getDescripcion(),
                usuario.getFechaRegistro(),
                resenaService.listar(null, id),
                hilos.findByUsuarioId(id).stream().map(foroService::convertirPublico).toList(),
                anotacionService.listarPublicas(id)
        );
    }

    public UsuarioPerfilDTO obtenerPerfilActual(String email) {
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("La sesión no corresponde a una cuenta disponible"));
        return obtenerPerfil(usuario.getPublicUsername());
    }

    @Transactional
    public UsuarioPerfilDTO actualizarPerfilActual(String email, UsuarioActualizacionDTO peticion) {
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("La sesión no corresponde a una cuenta disponible"));

        if (peticion.username() != null && !peticion.username().trim().isEmpty()) {
            String nuevoUsername = peticion.username().trim();
            if (nuevoUsername.length() < 3 || nuevoUsername.length() > 50) {
                throw new PeticionInvalidaException("El nombre de usuario debe tener entre 3 y 50 caracteres");
            }
            if (!nuevoUsername.equals(usuario.getPublicUsername())) {
                if (!nuevoUsername.equalsIgnoreCase(usuario.getPublicUsername()) && usuarios.existsByUsernameIgnoreCase(nuevoUsername)) {
                    throw new ConflictoException("El nombre de usuario ya está en uso");
                }
                usuario.setUsername(nuevoUsername);
            }
        }

        if (peticion.descripcion() != null) {
            String desc = peticion.descripcion().trim();
            if (desc.length() > 1000) {
                throw new PeticionInvalidaException("La descripción no puede superar los 1000 caracteres");
            }
            usuario.setDescripcion(desc.isEmpty() ? null : desc);
        }

        usuarios.save(usuario);
        return obtenerPerfil(usuario.getPublicUsername());
    }

    private UsuarioResumenDTO resumen(Usuario usuario) {
        return new UsuarioResumenDTO(usuario.getId(), usuario.getPublicUsername(), usuario.getDescripcion());
    }
}
