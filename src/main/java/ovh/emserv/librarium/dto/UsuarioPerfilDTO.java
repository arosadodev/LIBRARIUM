package ovh.emserv.librarium.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioPerfilDTO(
        UsuarioResumenDTO usuario,
        String descripcion,
        LocalDateTime fechaRegistro,
        List<ResenaRespuestaDTO> resenas,
        List<ForoHiloRespuestaDTO> hilos,
        List<AnotacionRespuestaDTO> anotaciones
) {
    public UsuarioPerfilDTO(
            UsuarioResumenDTO usuario,
            LocalDateTime fechaRegistro,
            List<ResenaRespuestaDTO> resenas,
            List<ForoHiloRespuestaDTO> hilos,
            List<AnotacionRespuestaDTO> anotaciones
    ) {
        this(usuario, usuario != null ? usuario.descripcion() : null, fechaRegistro, resenas, hilos, anotaciones);
    }
}
