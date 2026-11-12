package ovh.emserv.librarium.dto;

/**
 * @author mit
 * @date 9/11/26 20:12
 */
public record TokenRespuesta(String username, String rol) {
    // Mantiene compatibilidad de código fuente con consumidores antiguos; el JWT nunca se devuelve en JSON.
    public TokenRespuesta(String ignoredToken, String username, String rol) {
        this(username, rol);
    }
}
