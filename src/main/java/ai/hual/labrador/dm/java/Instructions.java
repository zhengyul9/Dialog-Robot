package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.Instruction;
import ai.hual.labrador.kg.KnowledgeAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Passed as a parameter to {@link IPolicyManager#process(Object, KnowledgeAccessor, Instructions)} for
 * dm implementation to pop instructions.
 * Created by Dai Wentao on 2017/7/5.
 */
public class Instructions {

    private List<Instruction> instructions = new ArrayList<>();

    public void add(Instruction instruction) {
        instructions.add(instruction);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

}
