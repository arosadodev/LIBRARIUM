package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.ForoHiloDTO;
import ovh.emserv.librarium.dto.ForoHiloRespuestaDTO;
import ovh.emserv.librarium.dto.ForoRespuestaDTO;
import ovh.emserv.librarium.dto.ForoRespuestaSalidaDTO;
import ovh.emserv.librarium.service.ForoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@RestController
@RequestMapping("/api/foros")
public class ForoController {
    private final ForoService service;

    public ForoController(ForoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ForoHiloRespuestaDTO> listar(@RequestParam(required = false) Long libroId) {
        return service.listar(libroId);
    }

    @PostMapping
    public ForoHiloRespuestaDTO crear(@Valid @RequestBody ForoHiloDTO dto, Authentication autenticacion) {
        return service.crear(dto, autenticacion.getName());
    }

    @PutMapping("/{id}")
    public ForoHiloRespuestaDTO actualizar(@PathVariable Long id, @Valid @RequestBody ForoHiloDTO dto,
                                            Authentication autenticacion) {
        return service.actualizarHilo(id, dto, autenticacion.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id, Authentication autenticacion) {
        service.borrarHilo(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{hiloId}/respuestas")
    public Page<ForoRespuestaSalidaDTO> respuestas(@PathVariable Long hiloId, Pageable pageable) {
        return service.respuestas(hiloId, pageable);
    }

    @PostMapping("/{hiloId}/respuestas")
    public ForoRespuestaSalidaDTO crearRespuesta(@PathVariable Long hiloId,
                                                  @Valid @RequestBody ForoRespuestaDTO dto,
                                                  Authentication autenticacion) {
        return service.crearRespuesta(hiloId, dto, autenticacion.getName());
    }

    @PutMapping("/respuestas/{id}")
    public ForoRespuestaSalidaDTO actualizarRespuesta(@PathVariable Long id,
                                                       @Valid @RequestBody ForoRespuestaDTO dto,
                                                       Authentication autenticacion) {
        return service.actualizarRespuesta(id, dto, autenticacion.getName());
    }

    @DeleteMapping("/respuestas/{id}")
    public ResponseEntity<Void> borrarRespuesta(@PathVariable Long id, Authentication autenticacion) {
        service.borrarRespuesta(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
