package ai.hual.labrador.nlu;


/**
 * Natural language understanding interface of dialog.
 * Created by Dai Wentao on 2017/6/26.
 */
public interface NLU {

    /**
     * Understand input and produce a list of {@link QueryAct} which is sorted
     * by score. Each element of the returned list is a possible understand of
     * input.
     *
     * @param input The input natural language
     * @return A list of QueryAct that presents the possible understands of
     * input.
     */
    NLUResult understand(String input);

}
