package ai.hual.labrador.dm.java;

import ai.hual.labrador.nlu.QueryAct;

import java.util.List;

/**
 * Interface of state manager, who updates state according to the input act hypotheses.
 * Created by Dai Wentao on 2017/6/27.
 */
public interface IStateManager {

    /**
     * Update state according to given DA possibilities.
     *
     * @param input Original input
     * @param hyps  Possible DAs
     * @param state State, which should be json serializable.
     */
    void updateState(String input, List<QueryAct> hyps, Object state);

}
