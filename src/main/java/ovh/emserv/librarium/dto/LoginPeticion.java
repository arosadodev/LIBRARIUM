package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * @author mit
 * @date 9/11/26 20:12
 */
public record LoginPeticion(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
