package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMResult;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.NLUResult;
import ai.hual.labrador.nlu.QueryAct;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of {@link DM} that uses given state manager and policy manager java instance
 * Created by Dai Wentao on 2017/6/27.
 *
 * @deprecated Use {@link HSMDM}
 */
@Deprecated
public class JavaDM implements DM {


    private final ObjectMapper mapper = new ObjectMapper();

    private final Class<?> contextClass;
    private final IStateManager stateManager;
    private final IPolicyManager policyManager;

    public JavaDM(IStateManager stateManager, IPolicyManager policyManager, Class<?> contextClass) {
        this.contextClass = contextClass;
        this.stateManager = stateManager;
        this.policyManager = policyManager;
    }

    @Override
    public DMResult process(String input, NLUResult nluResult, String strState) {
        List<QueryAct> hyps = nluResult.retrieveHyps();
        try {
            // parse JSON string state into state object
            Object context = mapper.readValue(strState, contextClass);

            // invoke update method of state manager
            stateManager.updateState(input, hyps, context);

            // invoke process method of policy manager
            Instructions instructions = new Instructions();
            ResponseAct answerAct = policyManager.process(context, instructions);

            return new DMResult(mapper.writeValueAsString(context), answerAct, instructions.getInstructions());
        } catch (IOException e) {
            throw new DMException("Error converting context JSON.", e);
        }
    }

    @Override
    public DMResult process(String input, NLUResult nluResult, String turnParams, String strState) {
        return process(input, nluResult, strState);
    }

    @Override
    public void close() {
    }

}
