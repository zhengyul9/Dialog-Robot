package ai.hual.labrador.dm;

import ai.hual.labrador.dialog.AccessorRepository;

import java.util.Map;

public interface Execution {

    /**
     * Set up Execution.
     *
     * @param params             params map
     * @param accessorRepository a repository of accessors providing accessors
     */
    void setUp(Map<String, ContextedString> params, AccessorRepository accessorRepository);

    /**
     * Execute.
     *
     * @param context context
     * @return {@link ExecutionResult}
     */
    ExecutionResult execute(Context context);
}
