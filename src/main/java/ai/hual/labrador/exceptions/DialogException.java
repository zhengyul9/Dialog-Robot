package ai.hual.labrador.exceptions;

public class DialogException extends RuntimeException {

    public DialogException(String msg) {
        super(msg);
    }

    public DialogException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
