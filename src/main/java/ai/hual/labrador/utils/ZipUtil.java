package ai.hual.labrador.utils;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Util to help read and put entry in zip
 * Created by Dai Wentao on 2017/7/6.
 */
public class ZipUtil {

    /**
     * Write all entries in the files map into a zip file.
     *
     * @param files A map with file name as key, and file body in byte array as value.
     * @return zip file presented by byte array.
     * @throws IOException when error occurs in writing zip.
     */
    public static byte[] putAllEntries(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(byteOut);
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String name = entry.getKey();
            byte[] body = entry.getValue();
            zip.putNextEntry(new ZipEntry(name));
            zip.write(body);
            zip.closeEntry();
        }
        zip.close();
        return byteOut.toByteArray();
    }

    /**
     * Read all zip entries in a zip file.
     *
     * @param zipBytes zip file presented by byte array.
     * @return A map with file name as key, and file body in byte array as value.
     * @throws IOException when error occurs in reading zip.
     */
    public static Map<String, byte[]> readAllEntries(byte[] zipBytes) throws IOException {
        Map<String, byte[]> result = new HashMap<>();

        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            ByteStreams.copy(zip, dataStream);
            result.put(entry.getName(), dataStream.toByteArray());
        }
        return result;
    }

}
