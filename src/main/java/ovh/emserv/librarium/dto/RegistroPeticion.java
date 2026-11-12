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
        @Size(min = 3, max = 50, message = "El nick debe tener entre 3 y 50 caracteres")
        @jakarta.validation.constraints.Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nick solo puede contener letras, números, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 15, message = "La contraseña debe tener al menos 15 caracteres")
        String password
) {
}
