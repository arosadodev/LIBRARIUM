package ovh.emserv.librarium.dto;

/**
 * @author mit
 * @date 9/11/26 20:12
 */
public record TokenRespuesta(String token, String username, String rol) {
    public TokenRespuesta(String token, String username) {
        this(token, username, "ROLE_USER");
    }
}
