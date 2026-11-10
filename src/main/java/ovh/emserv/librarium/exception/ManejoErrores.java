package ovh.emserv.librarium.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejoErrores {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException exception,
                                                   HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException exception,
                                                HttpServletRequest request) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return responder(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(PeticionInvalidaException.class)
    public ResponseEntity<ApiError> peticionInvalida(PeticionInvalidaException exception,
                                                      HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflictoDeNegocio(ConflictoException exception,
                                                        HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccesoDenegadoException exception,
                                                    HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ApiError> servicioExterno(ServicioExternoException exception,
                                                     HttpServletRequest request) {
        return responder(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credenciales(CredencialesInvalidasException exception,
                                                   HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonInvalido(HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo JSON no es válido", request);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> parametroFaltante(
            org.springframework.web.bind.MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Falta el parámetro: " + exception.getParameterName(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> conflicto(DataIntegrityViolationException exception,
                                               HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "La operación entra en conflicto con los datos existentes", request);
    }

    private ResponseEntity<ApiError> responder(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
