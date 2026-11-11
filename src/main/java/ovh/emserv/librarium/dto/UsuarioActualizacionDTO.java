package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.Size;

public record UsuarioActualizacionDTO(
        @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
        String username,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String descripcion
) {
}
