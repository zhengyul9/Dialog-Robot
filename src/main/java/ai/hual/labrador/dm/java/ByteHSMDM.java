package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Properties;

public class ByteHSMDM extends HSMDM {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File tempJarFile;
    private final URLClassLoader loader;

    public ByteHSMDM(DialogConfig dialogConfig, File tempJarFile, URLClassLoader loader,
                     AccessorRepository accessorRepository, Properties properties) {
        super(dialogConfig, loader, accessorRepository, properties);
        this.tempJarFile = tempJarFile;
        this.loader = loader;
    }

    public URLClassLoader getLoader() {
        return loader;
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
        if (!tempJarFile.delete()) {
            logger.info("Unable to delete temp jar file {}. Deleting on exit.", tempJarFile.getName());
        }
    }
}
