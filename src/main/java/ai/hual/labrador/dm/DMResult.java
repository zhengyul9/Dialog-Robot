package ai.hual.labrador.dm;

import ai.hual.labrador.nlg.ResponseAct;

import java.util.List;

/**
 * The result containing updated state and answer act for DM.
 * Created by Dai Wentao on 2017/6/27.
 */
public class DMResult {

    private String state;
    private ResponseAct act;
    private List<Instruction> instructions;

    public DMResult(String state, ResponseAct act, List<Instruction> instructions) {
        this.state = state;
        this.act = act;
        this.instructions = instructions;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ResponseAct getAct() {
        return act;
    }

    public void setAct(ResponseAct act) {
        this.act = act;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
}
