package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMModelType;
import ai.hual.labrador.dm.SerializableDMModel;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.utils.ZipUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static ai.hual.labrador.dm.DMModelType.HSM;

public class ByteHSMDMModel implements SerializableDMModel {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private byte[] jsonBytes;
    private byte[] jarBytes;

    public static final String NAME_CONFIG = "DialogConfig.json";
    public static final String NAME_JAR = "dm_class.jar";

    public ByteHSMDMModel() {
        this.jsonBytes = new byte[0];
        this.jarBytes = new byte[0];
    }

    /**
     * @param bytes bytes
     */
    public ByteHSMDMModel(byte[] bytes) {
        try {
            Map<String, byte[]> contents = ZipUtil.readAllEntries(bytes);
            jsonBytes = contents.get(NAME_CONFIG);
            jarBytes = contents.get(NAME_JAR);
        } catch (IOException e) {
            throw new DMException("Error reading zip in bytes.", e);
        }
    }

    @Override
    public DMModelType getType() {
        return HSM;
    }

    @Override
    public DM make(AccessorRepository accessorRepository, Properties properties) {
        try {
            DialogConfig dialogConfig;
            File tempJarFile;
            ObjectMapper mapper = new ObjectMapper();

            // deserialize DialogConfig from json file
            dialogConfig = mapper.readValue(jsonBytes, DialogConfig.class);
            tempJarFile = File.createTempFile("labrador-hsmdm-jar-temp-", ".jar");
            tempJarFile.deleteOnExit();
            Files.write(jarBytes, tempJarFile);

            // create class loader for jar
            URL[] url = new URL[]{tempJarFile.toURI().toURL()};
            URLClassLoader loader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () ->
                    new URLClassLoader(url, DM.class.getClassLoader()));

            // construct HSMDM
            return new ByteHSMDM(dialogConfig, tempJarFile, loader, accessorRepository, properties);
        } catch (NoClassDefFoundError | IOException e) {
            throw new DMException("Error loading zip", e);
        }
    }

    @Override
    public byte[] serialize() {
        Map<String, byte[]> zipMap = new HashMap<>();
        zipMap.put(NAME_CONFIG, jsonBytes);
        zipMap.put(NAME_JAR, jarBytes);
        try {
            return ZipUtil.putAllEntries(zipMap);
        } catch (IOException e) {
            throw new DMException("Error serializing files bytes into zip bytes.", e);
        }
    }

    public byte[] getJsonBytes() {
        return Arrays.copyOf(jsonBytes, jsonBytes.length);
    }

    public byte[] getJarBytes() {
        return Arrays.copyOf(jarBytes, jarBytes.length);
    }

    public void setJsonBytes(byte[] jsonBytes) {
        this.jsonBytes = Arrays.copyOf(jsonBytes, jsonBytes.length);
    }

    public void setJarBytes(byte[] jarBytes) {
        this.jarBytes = Arrays.copyOf(jarBytes, jarBytes.length);
    }
}
