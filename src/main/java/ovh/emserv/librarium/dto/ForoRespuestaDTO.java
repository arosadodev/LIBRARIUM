package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForoRespuestaDTO(@NotBlank @Size(max = 10000) String contenido) {
}
