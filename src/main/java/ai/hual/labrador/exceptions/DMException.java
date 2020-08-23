package ai.hual.labrador.exceptions;

public class DMException extends RuntimeException {

    public DMException(String msg) {
        super(msg);
    }

    public DMException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
