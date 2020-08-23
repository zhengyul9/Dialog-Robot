package ai.hual.labrador.dialog;

import ai.hual.labrador.dm.Instruction;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.NLUResult;

import java.util.List;

/**
 * The result of dialog system
 * Created by Dai Wentao on 2017/6/29.
 */
public class DialogResult {

    private String answer;
    private String state;
    private List<Instruction> instructions;
    private NLUResult nluResult;
    private ResponseAct responseAct;

    public DialogResult(String answer, String state, List<Instruction> instructions,
                        NLUResult nluResult, ResponseAct responseAct) {
        this.answer = answer;
        this.state = state;
        this.instructions = instructions;
        this.nluResult = nluResult;
        this.responseAct = responseAct;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public ResponseAct getResponseAct() {
        return responseAct;
    }

    public void setResponseAct(ResponseAct responseAct) {
        this.responseAct = responseAct;
    }

    public NLUResult getNluResult() {
        return nluResult;
    }

    public void setNluResult(NLUResult nluResult) {
        this.nluResult = nluResult;
    }
}
