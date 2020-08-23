package ai.hual.labrador.dm;

import ai.hual.labrador.dialog.AccessorRepository;

import java.util.Map;

/**
 * Interface for condition.
 */
public interface Condition {

    /**
     * Parameters for the condition.
     *
     * @param params             params needed
     * @param accessorRepository a repository of accessors providing accessors
     */
    void setUp(Map<String, ContextedString> params, AccessorRepository accessorRepository);

    /**
     * Tell if the condition has been met.
     *
     * @param context context
     * @return true if condition is met
     */
    boolean accept(Context context);
}
