package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMModel;
import ai.hual.labrador.dm.DMModelType;

import java.util.Properties;

import static ai.hual.labrador.dm.DMModelType.JAVA;

/**
 * A DMModel that make DM from given state manager and policy manager instance
 * Created by Dai Wentao on 2017/7/6.
 */
@Deprecated
public class JavaDMModel implements DMModel {

    private final Class<?> contextClass;
    private final IStateManager stateManager;
    private final IPolicyManager policyManager;

    public JavaDMModel(IStateManager stateManager, IPolicyManager policyManager, Class<?> contextClass) {
        this.contextClass = contextClass;
        this.stateManager = stateManager;
        this.policyManager = policyManager;
    }

    @Override
    public DMModelType getType() {
        return JAVA;
    }

    @Override
    public DM make(AccessorRepository accessorRepository, Properties properties) {
        return new JavaDM(stateManager, policyManager, contextClass);
    }
}
