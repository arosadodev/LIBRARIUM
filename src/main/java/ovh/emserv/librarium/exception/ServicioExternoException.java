package ovh.emserv.librarium.exception;

public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }

    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
