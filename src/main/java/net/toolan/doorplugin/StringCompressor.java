package net.toolan.doorplugin;

import java.io.*;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by jonathan on 24/03/2017.
 * Copied from http://stackoverflow.com/a/10572491/6910
 */
enum StringCompressor {
    ;
    public static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes("UTF-8"));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    public static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while((len = in.read(buffer))>0)
                baos.write(buffer, 0, len);
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String b64encode(String s) {
        try {
            byte[] encodedBytes = Base64.getEncoder().encode(s.getBytes("UTF-8"));
            return new String(encodedBytes, "UTF-8");
        } catch (Exception ex) {
            return ex.getMessage() + " => unable to encode";
        }
    }
    public static String b64decode(String s) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(s.getBytes("UTF-8"));
            return new String(decodedBytes, "UTF-8");
        } catch (Exception ex) {
            return ex.getMessage() + " => unable to decode";
        }
    }

    public static String byteToString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }
    public static byte[] stringToBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    public static String crunch(String s) {
        return b64encode(byteToString(compress(s)));
    }

    public static String decrunch(String s) {
        return decompress(stringToBytes(b64decode(s)));
    }
}
