package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.AnotacionDTO;
import ovh.emserv.librarium.dto.AnotacionRespuestaDTO;
import ovh.emserv.librarium.service.AnotacionService;
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
@RequestMapping("/api/anotaciones")
public class AnotacionController {
    private final AnotacionService service;

    public AnotacionController(AnotacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<AnotacionRespuestaDTO> listar(@RequestParam(required = false) Long libroId,
                                              @RequestParam(required = false) Long usuarioId) {
        return service.listar(libroId, usuarioId);
    }

    @PostMapping
    public AnotacionRespuestaDTO crear(@Valid @RequestBody AnotacionDTO dto, Authentication autenticacion) {
        return service.crear(dto, autenticacion.getName());
    }

    @PutMapping("/{id}")
    public AnotacionRespuestaDTO actualizar(@PathVariable Long id, @Valid @RequestBody AnotacionDTO dto,
                                             Authentication autenticacion) {
        return service.actualizar(id, dto, autenticacion.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id, Authentication autenticacion) {
        service.borrar(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
