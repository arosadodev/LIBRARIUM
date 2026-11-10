package ovh.emserv.librarium.dto;

import ovh.emserv.librarium.entity.Anotacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Data
public class AnotacionDTO {
    private Long id;
    private Integer capituloNumero;
    private Integer pagina;
    @NotNull
    private Anotacion.Tipo tipo;
    @NotBlank
    @Size(max = 10000)
    private String contenido;
    @NotNull
    private Long libroId;
}
