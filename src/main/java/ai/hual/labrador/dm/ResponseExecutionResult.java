package ai.hual.labrador.dm;

import ai.hual.labrador.nlg.ResponseAct;

import java.util.List;

public class ResponseExecutionResult implements ExecutionResult {

    private ResponseAct responseAct;
    private List<Instruction> instructions;

    public ResponseAct getResponseAct() {
        return responseAct;
    }

    public void setResponseAct(ResponseAct responseAct) {
        this.responseAct = responseAct;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
}
