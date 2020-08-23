package ai.hual.labrador.dm;

import ai.hual.labrador.dm.java.DialogConfig;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.utils.ZipUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static ai.hual.labrador.dm.java.ByteHSMDMModel.NAME_CONFIG;
import static ai.hual.labrador.dm.java.ByteHSMDMModel.NAME_JAR;

public class utils {

    /**
     * Helper function to load formEntries json as String.
     *
     * @return json as string
     */
    public static String loadFormEntriesJson() {
        new Config();
        String dialogConfigPath = Config.get("local_form_entries_json");
        try {
            return IOUtils.toString(Config.getLoader().getResourceAsStream(dialogConfigPath), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Helper function to get dialogConfig from json.
     *
     * @return dialogConfig
     */
    public static DialogConfig loadDialogConfigFromJson() {

        DialogConfig dialogConfig = null;
        ObjectMapper mapper = new ObjectMapper();

        new Config();
        String dialogConfigPath = Config.get("local_dialogconfig_json");
        try {
            String json = IOUtils.toString(Config.getLoader().getResourceAsStream(dialogConfigPath), "UTF-8");
            dialogConfig = mapper.readValue(json, DialogConfig.class);
        } catch (NoClassDefFoundError | IOException e) {
            System.out.println(e);
        }
        return dialogConfig;
    }

    /**
     * Helper function to load DialogConfig.
     *
     * @return DialogConfig
     */
    public static DialogConfig loadDialogConfigFromZip() {

        DialogConfig dialogConfig = null;

        new Config();
        String dmZipPath = Config.get("local_dm_zip");
        try {
            byte[] bytes = IOUtils.toByteArray(Config.getLoader().getResourceAsStream(dmZipPath));
            // create classloader from file
            File tempJarFile;
            ObjectMapper mapper = new ObjectMapper();

            Map<String, byte[]> contents = ZipUtil.readAllEntries(bytes);
            dialogConfig = mapper.readValue(contents.get(NAME_CONFIG), DialogConfig.class);
            tempJarFile = File.createTempFile("labrador-hsmdm-jar-temp-", ".jar");
            tempJarFile.deleteOnExit();
            Files.write(contents.get(NAME_JAR), tempJarFile);

        } catch (NoClassDefFoundError | IOException e) {
            System.out.println(e);
        }

        return dialogConfig;
    }

    /**
     * Helper function to get classLoader from jar.
     *
     * @return classLoader
     */
    public static URLClassLoader getClassLoaderFromJar() {

        URLClassLoader loader = null;

        new Config();
        String dmJarPath = Config.get("local_dm_class_jar");
        try {
            byte[] bytes = IOUtils.toByteArray(Config.getLoader().getResourceAsStream(dmJarPath));
            File tempJarFile = File.createTempFile("labrador-hsmdm-jar-temp-", ".jar");
            tempJarFile.deleteOnExit();
            Files.write(bytes, tempJarFile);

            // class loader of jar
            URL[] url = new URL[]{tempJarFile.toURI().toURL()};
            loader = new URLClassLoader(url, DM.class.getClassLoader());
        } catch (NoClassDefFoundError | IOException e) {
            System.out.println(e);
        }
        return loader;
    }

    /**
     * Helper function to get classLoader from jar in zip.
     *
     * @return class loader
     */
    public static URLClassLoader getClassLoaderFromZip() {

        URLClassLoader loader = null;

        new Config();
        String dmZipPath = Config.get("local_dm_zip");
        try {
            byte[] bytes = IOUtils.toByteArray(Config.getLoader().getResourceAsStream(dmZipPath));
            // create classloader from file
            File tempJarFile;

            Map<String, byte[]> contents = ZipUtil.readAllEntries(bytes);
            tempJarFile = File.createTempFile("labrador-hsmdm-jar-temp-", ".jar");
            tempJarFile.deleteOnExit();
            Files.write(contents.get(NAME_JAR), tempJarFile);
            // class loader of jar
            URL[] url = new URL[]{tempJarFile.toURI().toURL()};
            loader = new URLClassLoader(url, DM.class.getClassLoader());
        } catch (NoClassDefFoundError | IOException e) {
            System.out.println(e);
        }
        return loader;
    }
}
