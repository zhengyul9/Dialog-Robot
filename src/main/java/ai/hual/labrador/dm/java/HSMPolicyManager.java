package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.nlg.ResponseAct;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class HSMPolicyManager {

    private static final Logger logger = LoggerFactory.getLogger(HSMPolicyManager.class);

    public HSMPolicyManager() {
    }

    /**
     * Decide system action and return answer DA
     *
     * @param context    context
     * @param executions list of executions
     * @return Answer DA
     */
    ResponseAct process(Context context, List<Execution> executions, Instructions instructions) {
        ResponseAct responseAct = null;
        for (Execution execution : executions) {
            logger.debug("Executing {}", execution.getClass().getSimpleName());
            ExecutionResult executionResult = execution.execute(context);
            if (executionResult instanceof ResponseExecutionResult) {
                ResponseExecutionResult responseExecutionResult = (ResponseExecutionResult) executionResult;
                responseAct = ObjectUtils.firstNonNull(responseAct, responseExecutionResult.getResponseAct());
                Optional.ofNullable(responseExecutionResult.getInstructions()).ifPresent(x -> x.forEach(instructions::add));
            }
        }
        return responseAct;
    }

}
