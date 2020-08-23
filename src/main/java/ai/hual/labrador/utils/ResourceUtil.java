package ai.hual.labrador.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.jar.JarFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

public class ResourceUtil {


    private static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);

    /**
     * List directory contents for a resource folder. Not recursive. This is
     * basically a brute-force implementation. Works for regular files and also
     * JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources
     *              you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String[] getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException {
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        URL dirURL = clazz.getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL != null && dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            // "file:/xxx/xxx.jar!/xxx/xxx.jar!/xxx/xxx"
            String[] jarPaths = dirURL.getPath().substring("file:".length()).split("!");
            logger.debug("jarPaths: {}", String.join(", ", jarPaths));

            JarFile jar = new JarFile(new File(URLDecoder.decode(jarPaths[0], "UTF-8")));
            for (int i = 1; i < jarPaths.length - 1; i++) {
                String jarPath = jarPaths[i];
                if (jarPath.startsWith("/")) {
                    jarPath = jarPath.substring(1); // spring-boot-loader JarFile entries do not start with "/"
                }
                jar = jar.getNestedJarFile(jar.getJarEntry(jarPath));
            }

            Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = "/" + entry.getName();
                if (name.startsWith(path) && !name.equals(path)) { // filter according to the path
                    result.add(name.substring(path.length()));
                }
            }
            logger.debug("result: {}", result);
            jar.close();
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
