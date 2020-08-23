package ai.hual.labrador.nlu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Created by ethan on 17-6-27.
 */
public class Config {
    private static final String CONFIG_FILE_PATH = "config.properties";
    private static Properties prop = new Properties();
    ;

    /* get a loader to make it convenient to access file under classes root path */
    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    static {
        //load a properties file from class path, inside static method
        try (InputStreamReader input = new InputStreamReader(
                loader.getResourceAsStream(CONFIG_FILE_PATH), StandardCharsets.UTF_8)) {
            prop.load(input);
        } catch (IOException ex) {
            System.out.println(ex.toString());
            System.out.println("Could not find file " + CONFIG_FILE_PATH);
        }
    }

    public static String get(String propName) {
        String result = prop.getProperty(propName);
        if (result == null) System.out.println("Could not find property " + propName);
        return result;
    }

    public static ClassLoader getLoader() {

        return loader;
    }
}
