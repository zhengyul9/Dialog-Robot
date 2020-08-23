package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.nlu.QueryAct;

import java.util.Map;

public class QueryUpdateStrategy implements SlotUpdateStrategy {

    @Override
    public void setUp(String s, Map<String, ContextedString> map, AccessorRepository accessorRepository) {

    }

    @Override
    public Object update(QueryAct act, Object obj, Context context) {
        return act.getQuery();
    }

}
