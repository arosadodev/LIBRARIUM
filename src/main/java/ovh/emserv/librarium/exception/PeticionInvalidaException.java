package ovh.emserv.librarium.exception;

public class PeticionInvalidaException extends RuntimeException {
    public PeticionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
