package ai.hual.labrador.dm;

import ai.hual.labrador.nlu.NLUResult;

/**
 * Dialog manager interface of dialog.
 * Created by Dai Wentao on 2017/6/27.
 */
public interface DM {

    /**
     * @param input     Original input
     * @param nluResult NLU result pack
     * @param strState  State json as a string
     * @return result of DM
     * @deprecated See {@link #process(String, NLUResult, String, String)}
     */
    // TODO: remove KnowledgeAccessor
    @Deprecated
    DMResult process(String input, NLUResult nluResult, String strState);

    /**
     * @param input      Original input
     * @param nluResult  NLU result pack
     * @param turnParams params pass in this turn. serialized as json string
     * @param strState   State json as a string
     * @return result of DM
     */
    // TODO: remove KnowledgeAccessor
    DMResult process(String input, NLUResult nluResult, String turnParams, String strState);

    /**
     * release resources created by dm
     */
    void close();

}
