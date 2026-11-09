package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author mit
 * @date 9/11/26 20:11
 */
public record RegistroPeticion(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
        String password
) {
}
