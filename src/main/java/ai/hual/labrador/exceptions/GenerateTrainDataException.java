package ai.hual.labrador.exceptions;

public class GenerateTrainDataException extends IllegalArgumentException {

    public GenerateTrainDataException(String msg) {
        super(msg);
    }

    public GenerateTrainDataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
