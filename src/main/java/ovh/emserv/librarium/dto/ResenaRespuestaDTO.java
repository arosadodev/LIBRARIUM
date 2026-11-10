package ovh.emserv.librarium.dto;

import java.time.LocalDateTime;

public record ResenaRespuestaDTO(
        Long id,
        Integer puntuacion,
        String comentario,
        LocalDateTime fechaRegistro,
        Long libroId,
        UsuarioResumenDTO usuario
) {
}
