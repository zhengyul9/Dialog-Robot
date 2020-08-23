package ai.hual.labrador.exceptions;

public class NLUException extends RuntimeException {

    public NLUException(String msg) {
        super(msg);
    }

    public NLUException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
