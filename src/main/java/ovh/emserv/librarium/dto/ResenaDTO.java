package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public record ResenaDTO(
        @NotNull @Min(1) @Max(5) Integer puntuacion,
        @Size(max = 5000) String comentario,
        @NotNull Long libroId
) {}
