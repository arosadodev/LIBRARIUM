package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.ForoHiloDTO;
import ovh.emserv.librarium.entity.ForoHilo;
import ovh.emserv.librarium.entity.ForoRespuesta;
import ovh.emserv.librarium.service.ForoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foros")
public class ForoController {
    private final ForoService service;

    public ForoController(ForoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ForoHilo> listar(@RequestParam(required = false) Long libroId) {
        return service.listar(libroId);
    }

    @PostMapping
    public ForoHilo crear(@Valid @RequestBody ForoHiloDTO dto) {
        return service.crear(dto);
    }

    @GetMapping("/{hiloId}/respuestas")
    public List<ForoRespuesta> respuestas(@PathVariable Long hiloId) {
        return service.respuestas(hiloId);
    }
}
