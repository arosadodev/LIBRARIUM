package ovh.emserv.librarium.controller;

import ovh.emserv.librarium.dto.ResenaDTO;
import ovh.emserv.librarium.entity.Resena;
import ovh.emserv.librarium.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Resena> crearResena(@Valid @RequestBody ResenaDTO dto, Authentication autenticacion) {
        // authentication.getName() extrae el email del usuario desde el JWT validado
        String emailUsuarioLogueado = autenticacion.getName();

        Resena nuevaResena = resenaService.crearResenaSegura(dto, emailUsuarioLogueado);
        return ResponseEntity.ok(nuevaResena);
    }
}
