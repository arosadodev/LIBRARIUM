package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.AnotacionDTO;
import ovh.emserv.librarium.entity.Anotacion;
import ovh.emserv.librarium.service.AnotacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anotaciones")
public class AnotacionController {
    private final AnotacionService service;

    public AnotacionController(AnotacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Anotacion> listar(@RequestParam(required = false) Long libroId) {
        return service.listar(libroId);
    }

    @PostMapping
    public Anotacion crear(@Valid @RequestBody AnotacionDTO dto) {
        return service.crear(dto);
    }
}
