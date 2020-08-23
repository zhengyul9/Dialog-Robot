package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.nlu.QueryAct;

import java.util.List;
import java.util.Map;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.PLAIN_FORM;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.RANGE_DROPDOWN;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.TAG_FORM;

public class KGSparqlUpdateStrategy implements SlotUpdateStrategy {

    private static final String ONE_AS_LIST = "SELECT_ONE_AS_LIST";
    private static final String SELECT = "SELECT";
    private static final String OBJECT_AS_LIST = "SELECT_OBJECT_AS_LIST";

    @Param(tip = "Sparql")
    private ContextedString sparql;

    @Param(tip = "Sparql的参数，要与Sparql中的\"%s\"一一对应", component = TAG_FORM, required = false)
    private ContextedString sparqlParams;

    @Param(tip = "Sparql选择方法", defaultValue = ONE_AS_LIST, range = {ONE_AS_LIST, SELECT, OBJECT_AS_LIST}, component = RANGE_DROPDOWN)
    private ContextedString selectMethod;

    @Param(tip = "Sparql要查的字段，只有使用selectOneAsList方法时需要填", component = PLAIN_FORM, required = false)
    private ContextedString fieldOfSparql;

    @Param(tip = "Sparql查到的对象类型，只有使用selectObjectAsList方法时需要填", component = PLAIN_FORM, required = false)
    private ContextedString classOfSparqlObject;

    private KnowledgeAccessor knowledgeAccessor;

    @Override
    public void setUp(String slotName, Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        knowledgeAccessor = accessorRepository.getKnowledgeAccessor();
        sparql = params.get("sparql");
        sparqlParams = params.get("sparqlParams");
        selectMethod = params.get("selectMethod");
        fieldOfSparql = params.get("fieldOfSparql");
        classOfSparqlObject = params.get("classOfSparqlObject");
    }

    @Override
    public Object update(QueryAct act, Object original, Context context) {
        if (original != null)
            return original;
        String strSparql = sparql.render(context);

        // format sparql if has param
        String formattedSparql;
        if (sparqlParams != null) {
            List<String> sparqlParamsList = sparqlParams.renderToList(context);
            formattedSparql = String.format(strSparql, (Object[]) sparqlParamsList.toArray());
        } else
            formattedSparql = strSparql;

        String strSelectMethod = selectMethod.render(context);
        switch (strSelectMethod) {
            case ONE_AS_LIST:
                if (fieldOfSparql == null)
                    throw new DMException("Sparql的查询字段未填");
                String strFieldOfSparql = fieldOfSparql.render(context);
                return knowledgeAccessor.selectOneAsList(formattedSparql, strFieldOfSparql);
            case OBJECT_AS_LIST:
                if (classOfSparqlObject == null)
                    throw new DMException("Sparql的查询出的对象类型未填");
                String strObjectOfSparql = classOfSparqlObject.render(context);
                try {
                    return knowledgeAccessor.selectObjectAsList(formattedSparql, Class.forName(strObjectOfSparql));
                } catch (ClassNotFoundException e) {
                    throw new DMException("未找到类：" + strObjectOfSparql);
                }
            case SELECT:
                return knowledgeAccessor.select(formattedSparql);
            default:
                throw new DMException("Something wrong with select method");
        }
    }
}
