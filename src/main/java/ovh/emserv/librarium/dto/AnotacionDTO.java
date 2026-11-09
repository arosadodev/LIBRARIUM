package ovh.emserv.librarium.dto;

import ovh.emserv.librarium.entity.Anotacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String contenido;
    @NotNull
    private Long libroId;
    @NotNull
    private Long usuarioId;
}
