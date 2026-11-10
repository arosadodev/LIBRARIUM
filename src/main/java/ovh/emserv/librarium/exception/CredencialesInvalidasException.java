package ovh.emserv.librarium.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales incorrectas");
    }
}
