package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.ConditionConfig;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.hsm.ConfigUtils;
import ai.hual.labrador.exceptions.DMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DMTransition {

    private static final Logger logger = LoggerFactory.getLogger(DMTransition.class);

    /**
     * Destination state name.
     */
    private String to;

    /**
     * Conditions must be met to complete the transition.
     */
    private List<Condition> conditions;

    /**
     * Executions after conditions all met.
     */
    private List<Execution> executions;

    private DMTransitionConfig transitionConfig;

    public DMTransition(DMTransitionConfig transitionConfig, ClassLoader classLoader,
                        AccessorRepository accessorRepository) {
        this.to = transitionConfig.getTo();
        this.conditions = conditionsByConfig(transitionConfig, classLoader, accessorRepository);
        this.executions = ConfigUtils.executionsByConfig(transitionConfig.getExecutions(), classLoader, accessorRepository);
        this.transitionConfig = transitionConfig;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * Get list of Condition by transitionConfig.
     *
     * @param transitionConfig transition configuration
     * @param classLoader      class loader containing Condition's implementations
     * @return list of Condition
     */
    public List<Condition> conditionsByConfig(DMTransitionConfig transitionConfig, ClassLoader classLoader,
                                              AccessorRepository accessorRepository) {
        List<Condition> conditions = new ArrayList<>();
        for (ConditionConfig config : transitionConfig.getConditions()) {
            try {
                Condition condition = (Condition) Class.forName(
                        config.getCondition(), true, classLoader).newInstance();
                condition.setUp(config.paramsAsMap(), accessorRepository);
                conditions.add(condition);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new DMException("Class " + config.getCondition() + " not found.");
            }
        }
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Check all the conditions in condition list have been met.
     *
     * @param context context used to test the condition
     * @return true if all conditions accepted
     */
    protected boolean acceptAllConditions(Context context) {
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);
            boolean result = condition.accept(context);
            boolean reverse = transitionConfig.getConditions().get(i).isReverser();
            logger.debug("Condition {}: {}", condition.getClass().getSimpleName(), result);
            if (!(result ^ reverse)) {
                return false;
            }
        }
        return true;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Execution> executions) {
        this.executions = executions;
    }
}
