package ai.hual.labrador.exceptions;

/**
 * A runtime exception in knowledge operations.
 * Created by Dai Wentao on 2017/5/18.
 */
public class KnowledgeException extends RuntimeException {

    public KnowledgeException(String msg) {
        super(msg);
    }

    public KnowledgeException(Exception e) {
        super(e);
    }

    public KnowledgeException(String msg, Exception e) {
        super(msg, e);
    }

}
