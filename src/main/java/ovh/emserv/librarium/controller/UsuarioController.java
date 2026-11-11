package ovh.emserv.librarium.controller;

import jakarta.validation.Valid;
import ovh.emserv.librarium.dto.UsuarioActualizacionDTO;
import ovh.emserv.librarium.dto.UsuarioPerfilDTO;
import ovh.emserv.librarium.dto.UsuarioResumenDTO;
import ovh.emserv.librarium.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResumenDTO> listar(@RequestParam(required = false) String q) {
        return service.buscar(q);
    }

    @GetMapping("/buscar")
    public List<UsuarioResumenDTO> buscar(@RequestParam String q) {
        return service.buscar(q);
    }

    @GetMapping("/me")
    public UsuarioPerfilDTO miPerfil(Authentication autenticacion) {
        return service.obtenerPerfilActual(autenticacion.getName());
    }

    @PutMapping("/me")
    public UsuarioPerfilDTO actualizarMiPerfil(Authentication autenticacion, @Valid @RequestBody UsuarioActualizacionDTO peticion) {
        return service.actualizarPerfilActual(autenticacion.getName(), peticion);
    }

    @PatchMapping("/me")
    public UsuarioPerfilDTO modificarMiPerfil(Authentication autenticacion, @Valid @RequestBody UsuarioActualizacionDTO peticion) {
        return service.actualizarPerfilActual(autenticacion.getName(), peticion);
    }

    @GetMapping("/{username}")
    public UsuarioPerfilDTO perfil(@PathVariable String username) {
        return service.obtenerPerfil(username);
    }
}
