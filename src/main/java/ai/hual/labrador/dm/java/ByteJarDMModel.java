package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMModelType;
import ai.hual.labrador.dm.SerializableDMModel;
import ai.hual.labrador.exceptions.DMException;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Properties;

import static ai.hual.labrador.dm.DMModelType.JAVA;
import static ai.hual.labrador.dm.java.ByteJarJavaDM.CONTEXT_CLASS_NAME;
import static ai.hual.labrador.dm.java.ByteJarJavaDM.POLICY_MANAGER_CLASS_NAME;
import static ai.hual.labrador.dm.java.ByteJarJavaDM.STATE_MANAGER_CLASS_NAME;

/**
 * A DMModel that make DM from a byte array presented jarBytes.
 * Created by Dai Wentao on 2017/7/6.
 */
@Deprecated
public class ByteJarDMModel implements SerializableDMModel {

    private byte[] jarBytes;

    /**
     * @param content Jar byte array.
     */
    public ByteJarDMModel(byte[] content) {
        this.jarBytes = Arrays.copyOf(content, content.length);
    }

    @Override
    public DMModelType getType() {
        return JAVA;
    }

    @Override
    public DM make(AccessorRepository accessorRepository, Properties properties) {
        try {
            // create classloader from file
            File tempFile = File.createTempFile("labrador-dm-tmp-", ".jar");
            tempFile.deleteOnExit();
            Files.write(jarBytes, tempFile);
            URL[] url = new URL[]{tempFile.toURI().toURL()};
            URLClassLoader loader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () ->
                    new URLClassLoader(url, DM.class.getClassLoader()));

            // get classes and instances
            Class<?> stateClass = Class.forName(CONTEXT_CLASS_NAME, true, loader);
            IStateManager stateManager = (IStateManager) tryNewInstanceWithProperties(
                    loader, STATE_MANAGER_CLASS_NAME, properties);
            IPolicyManager policyManager = (IPolicyManager) tryNewInstanceWithProperties(
                    loader, POLICY_MANAGER_CLASS_NAME, properties);

            // construct DM
            return new ByteJarJavaDM(stateManager, policyManager, stateClass, tempFile, loader);
        } catch (NoClassDefFoundError | IOException | ClassNotFoundException |
                IllegalAccessException | InstantiationException e) {
            throw new DMException("Error loading jar", e);
        }
    }

    private Object tryNewInstanceWithProperties(ClassLoader loader, String className, Properties properties)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            return Class.forName(className, true, loader).getDeclaredConstructor(Properties.class)
                    .newInstance(properties);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                InstantiationException e) {
            return Class.forName(className, true, loader).newInstance();
        }
    }

    @Override
    public byte[] serialize() {
        return Arrays.copyOf(jarBytes, jarBytes.length);
    }
}
