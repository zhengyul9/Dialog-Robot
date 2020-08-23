package ai.hual.labrador.dm;

import java.util.HashMap;
import java.util.Map;

/**
 * Instruction of result.
 * Created by Dai Wentao on 2017/7/5.
 */
public class Instruction {

    /**
     * The type of the instruction
     */
    private String type;

    /**
     * The parameters of instruction.
     */
    private Map<String, Object> params;

    /**
     * initialize instruction with type
     *
     * @param type The type of the instruction
     */
    public Instruction(String type) {
        this.type = type;
        params = new HashMap<>();
    }

    /**
     * Add a parameter to the instruction
     *
     * @param name  the name of the param
     * @param value the value of the param
     */
    public Instruction addParam(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
