package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Data
public class ForoHiloDTO {
    @NotBlank
    @Size(max = 200)
    private String tituloHilo;
    @NotBlank
    @Size(max = 10000)
    private String contenidoInicial;
    @NotNull
    private Long libroId;
}
