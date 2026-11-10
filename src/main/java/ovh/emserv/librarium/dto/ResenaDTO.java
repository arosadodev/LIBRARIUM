package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public record ResenaDTO(
        @NotNull @Min(1) @Max(5) Integer puntuacion,
        String comentario,
        @NotNull Long libroId
) {}
