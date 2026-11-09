package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Data
public class ResenaDTO {
    private Long id;
    @NotNull @Min(1) @Max(5)
    private Integer puntuacion;
    private String comentario;
    @NotNull
    private Long libroId;
    @NotNull
    private Long usuarioId;
}
