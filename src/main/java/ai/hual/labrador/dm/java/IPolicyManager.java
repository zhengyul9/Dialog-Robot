package ai.hual.labrador.dm.java;

import ai.hual.labrador.nlg.ResponseAct;

/**
 * Interface of policy manager, who produces answer act according to the state.
 * Created by Dai Wentao on 2017/6/27.
 */
public interface IPolicyManager {

    /**
     * Decide system action and return answer DA
     *
     * @param state        State, which should be json serializable.
     * @param instructions Instruction collector that holds instruction list.
     * @return Answer DA
     */
    ResponseAct process(Object state, Instructions instructions);

}
