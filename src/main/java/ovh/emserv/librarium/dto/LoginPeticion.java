package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author mit
 * @date 9/11/26 20:12
 */
public record LoginPeticion(
        @NotBlank @Size(max = 50) String username,
        @NotBlank String password) {
}
