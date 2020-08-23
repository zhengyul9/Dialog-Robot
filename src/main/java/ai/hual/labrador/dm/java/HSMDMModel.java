package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMModel;
import ai.hual.labrador.dm.DMModelType;

import java.util.Properties;

import static ai.hual.labrador.dm.DMModelType.HSM;

public class HSMDMModel implements DMModel {

    private final DialogConfig dialogConfig;
    private final ClassLoader classLoader;

    public HSMDMModel(DialogConfig dialogConfig, ClassLoader classLoader) {
        this.dialogConfig = dialogConfig;
        this.classLoader = classLoader;
    }

    @Override
    public DMModelType getType() {
        return HSM;
    }

    @Override
    public DM make(AccessorRepository accessorRepository, Properties properties) {
        return new HSMDM(dialogConfig, classLoader, accessorRepository, properties);
    }
}
