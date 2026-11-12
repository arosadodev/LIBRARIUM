package ovh.emserv.librarium.dto;

public record UsuarioResumenDTO(Long id, String username, String descripcion, String email) {
    public UsuarioResumenDTO(Long id, String username, String descripcion) {
        this(id, username, descripcion, null);
    }

    public UsuarioResumenDTO(Long id, String username) {
        this(id, username, null, null);
    }
}
