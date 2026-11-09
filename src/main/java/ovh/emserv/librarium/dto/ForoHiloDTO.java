package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Data
public class ForoHiloDTO {
    private Long id;
    @NotBlank
    private String tituloHilo;
    @NotBlank
    private String contenidoInicial;
    @NotNull
    private Long libroId;
    @NotNull
    private Long usuarioId;
}
