package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.Instruction;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.constants.SystemIntents;

import java.util.ArrayList;
import java.util.List;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_FAQ_NAME;

/**
 * Used only for giving response
 */
public class DefaultFAQExecution extends Parameterized implements Execution {

    @Override
    public ExecutionResult execute(Context context) {

        // if this execution is called, faqAnswer must not be null
        FaqAnswer faqAnswer = (FaqAnswer) context.slotContentByName(SYSTEM_FAQ_NAME);

        ResponseAct responseAct;
        responseAct = new ResponseAct(SystemIntents.FAQ_INTENT)
                .put(SystemIntents.FAQ_SLOT_RESULT, faqAnswer.getAnswer())
                .put(SystemIntents.FAQ_SLOT_QUESTION, faqAnswer.getStandardQuestion());

        ResponseExecutionResult result = new ResponseExecutionResult();
        result.setResponseAct(responseAct);

        List<Instruction> instructions = new ArrayList<>();
        instructions.add(new Instruction("msginfo_faq_a")
                .addParam("question", faqAnswer.getStandardQuestion())
                .addParam("score", faqAnswer.getConfidence()));
        result.setInstructions(instructions);

        return result;
    }
}
