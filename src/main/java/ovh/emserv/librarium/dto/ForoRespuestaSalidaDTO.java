package ovh.emserv.librarium.dto;

import java.time.LocalDateTime;

public record ForoRespuestaSalidaDTO(
        Long id,
        String contenido,
        LocalDateTime fechaCreacion,
        Long hiloId,
        UsuarioResumenDTO usuario
) {
}
