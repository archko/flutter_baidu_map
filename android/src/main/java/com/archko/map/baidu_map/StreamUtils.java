package com.archko.map.baidu_map;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;

/**
 * author archko
 */
public class StreamUtils {

    public static final String readStringFromInputStream(InputStream in) {
        if (in == null) {
            return "";
        }
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int len;
            while ((len = in.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return new String(bos.toByteArray(), "UTF-8");
        } catch (Exception e) {
        } finally {
            closeStream(bos);
        }
        return "";
    }

    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }
}
