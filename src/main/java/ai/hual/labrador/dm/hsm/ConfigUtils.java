package ai.hual.labrador.dm.hsm;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionConfig;
import ai.hual.labrador.dm.java.DMTransition;
import ai.hual.labrador.dm.java.DMTransitionConfig;
import ai.hual.labrador.exceptions.DMException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigUtils {

    /**
     * Set transitions by transitions configuration.
     *
     * @param transitionConfigs transitions configuration
     * @param classLoader       class loader containing implementations
     */
    public static List<DMTransition> transitionsByConfig(List<DMTransitionConfig> transitionConfigs,
                                                         ClassLoader classLoader,
                                                         AccessorRepository accessorRepository) {
        List<DMTransition> transitions = new ArrayList<>();
        for (DMTransitionConfig config : transitionConfigs) {
            DMTransition transition = new DMTransition(config, classLoader, accessorRepository);
            transitions.add(transition);
        }
        return transitions;
    }

    /**
     * Set executions by execution configuration.
     *
     * @param executionConfigs execution configuration
     * @param classLoader      class loader containing implementations
     */
    public static List<Execution> executionsByConfig(List<ExecutionConfig> executionConfigs, ClassLoader classLoader,
                                                     AccessorRepository accessorRepository) {
        if (executionConfigs == null)
            return new ArrayList<>();
        return executionConfigs.stream()
                .map(config -> executionByConfig(config, classLoader, accessorRepository))
                .collect(Collectors.toList());
    }

    /**
     * Set execution by execution configuration.
     *
     * @param config      execution configuration
     * @param classLoader class loader containing implementations
     */
    public static Execution executionByConfig(ExecutionConfig config, ClassLoader classLoader,
                                              AccessorRepository accessorRepository) {
        try {
            Execution execution = (Execution) Class.forName(
                    config.getExecution(), true, classLoader).newInstance();
            execution.setUp(config.paramsAsMap(), accessorRepository);
            return execution;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new DMException("Class " + config.getExecution() + " not found");
        }
    }

}
