package ovh.emserv.librarium.dto;

import ovh.emserv.librarium.entity.Anotacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
