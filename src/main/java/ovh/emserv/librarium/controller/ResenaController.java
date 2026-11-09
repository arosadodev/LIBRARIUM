package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.entity.Resena;
import ovh.emserv.librarium.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {
    private final ResenaService service;

    public ResenaController(ResenaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resena> listar(@RequestParam(required = false) Long libroId) {
        return service.listar(libroId);
    }

    @PostMapping
    public Resena crear(@Valid @RequestBody ResenaDTO dto) {
        return service.crear(dto);
    }
}
