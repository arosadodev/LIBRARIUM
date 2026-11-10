package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.dto.ResenaRespuestaDTO;
import ovh.emserv.librarium.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@RestController
@RequestMapping("/api/resenas")
public class ResenaController {
    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @PostMapping
    public ResponseEntity<ResenaRespuestaDTO> crearResena(@Valid @RequestBody ResenaDTO dto, Authentication autenticacion) {
        // authentication.getName() extrae el email del usuario desde el JWT validado
        String emailUsuarioLogueado = autenticacion.getName();

        ResenaRespuestaDTO nuevaResena = resenaService.crearResenaSegura(dto, emailUsuarioLogueado);
        return ResponseEntity.ok(nuevaResena);
    }

    @GetMapping
    public List<ResenaRespuestaDTO> listar(@RequestParam(required = false) Long libroId,
                                           @RequestParam(required = false) Long usuarioId) {
        return resenaService.listar(libroId, usuarioId);
    }

    @PutMapping("/{id}")
    public ResenaRespuestaDTO actualizar(@PathVariable Long id, @Valid @RequestBody ResenaDTO dto,
                                         Authentication autenticacion) {
        return resenaService.actualizar(id, dto, autenticacion.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id, Authentication autenticacion) {
        resenaService.borrarResena(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
