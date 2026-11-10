package ovh.emserv.librarium.dto;

import java.time.LocalDateTime;

public record ForoHiloRespuestaDTO(
        Long id,
        String tituloHilo,
        String contenidoInicial,
        LocalDateTime fechaCreacion,
        Long libroId,
        UsuarioResumenDTO usuario
) {
}
