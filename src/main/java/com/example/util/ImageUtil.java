package com.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class ImageUtil {

    public static final String PNG_MIME = "image/png";
    public static final String JPEG_MIME = "image/jpeg";

    public static final String TRANSPARENT_PX_DATA_URI =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO3f0CkAAAAASUVORK5CYII=";

    private ImageUtil() {}

    public static boolean isValidPng(byte[] bytes) {
        // PNG signature (first 4 bytes): 89 50 4E 47
        return bytes != null && bytes.length >= 4
                && (bytes[0] & 0xFF) == 0x89
                && (bytes[1] & 0xFF) == 0x50
                && (bytes[2] & 0xFF) == 0x4E
                && (bytes[3] & 0xFF) == 0x47;
    }

    public static boolean isValidJpeg(byte[] bytes) {
        // JPEG magic: FF D8 FF (accept any following marker, including E0 for JFIF)
        if (bytes == null || bytes.length < 3) return false;
        if ((bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF) {
            return false;
        }
        return true;
    }

    public static byte[] tryReadFile(java.nio.file.Path p) {
        try {
            if (p == null) return null;
            if (!Files.isRegularFile(p)) return null;
            return Files.readAllBytes(p);
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] readResource(ClassLoader cl, String path) {
        try (InputStream in = cl.getResourceAsStream(path.startsWith("/") ? path.substring(1) : path)) {
            if (in == null) return null;
            return in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public static String toDataUri(byte[] bytes, String mime) {
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
        return "data:" + mime + ";base64," + base64;
    }
}
