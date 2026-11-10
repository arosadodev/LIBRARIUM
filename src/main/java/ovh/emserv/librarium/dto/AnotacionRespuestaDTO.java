package ovh.emserv.librarium.dto;

import ovh.emserv.librarium.entity.Anotacion;

import java.time.LocalDateTime;

public record AnotacionRespuestaDTO(
        Long id,
        Integer capituloNumero,
        Integer pagina,
        Anotacion.Tipo tipo,
        String contenido,
        LocalDateTime fechaCreacion,
        Long libroId,
        UsuarioResumenDTO usuario
) {
}
