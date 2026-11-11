package ovh.emserv.librarium.dto;

public record UsuarioResumenDTO(Long id, String username, String descripcion) {
    public UsuarioResumenDTO(Long id, String username) {
        this(id, username, null);
    }
}
