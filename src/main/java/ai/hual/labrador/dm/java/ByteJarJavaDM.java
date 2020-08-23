package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * A {@link JavaDM} that initialize state manager and policy manager from byte array of a jar,
 * which is made in {@link ByteJarDMModel#make(AccessorRepository, Properties)}
 * Created by Dai Wentao on 2017/7/6.
 * @deprecated See {@link JavaDM}
 */
@Deprecated
public class ByteJarJavaDM extends JavaDM {

    static final String CONTEXT_CLASS_NAME = "dm.Context";
    static final String STATE_MANAGER_CLASS_NAME = "dm.StateManager";
    static final String POLICY_MANAGER_CLASS_NAME = "dm.PolicyManager";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File tempFile;
    private final URLClassLoader loader;

    ByteJarJavaDM(IStateManager stateManager, IPolicyManager policyManager, Class<?> contextClass,
                  File tempFile, URLClassLoader loader) {
        super(stateManager, policyManager, contextClass);
        this.tempFile = tempFile;
        this.loader = loader;
    }

    @Override
    public void close() {
        // release loader
        try {
            loader.close();
        } catch (IOException e) {
            logger.info("Unable to close ClassLoader for ByteJarJavaDM.");
        }

        // delete temp file
        if (!tempFile.delete()) {
            logger.info("Unable to delete temp file {}. Deleting on exit.", tempFile.getName());
        }
    }

}
