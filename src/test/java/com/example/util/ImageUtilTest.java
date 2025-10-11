package com.example.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilTest {

    @Test
    void isValidPng_detectsSignature() {
        byte[] valid = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x00};
        byte[] invalid = new byte[] {0x00, 0x00, 0x00, 0x00};
        assertTrue(ImageUtil.isValidPng(valid));
        assertFalse(ImageUtil.isValidPng(null));
        assertFalse(ImageUtil.isValidPng(new byte[0]));
        assertFalse(ImageUtil.isValidPng(invalid));
    }

    @Test
    void isValidJpeg_detectsMagic() {
        byte[] validShort = new byte[] {(byte)0xFF, (byte)0xD8, (byte)0xFF};
        byte[] validJfif = new byte[] {(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0};
        byte[] invalid = new byte[] {0x00, 0x00, 0x00};
        assertTrue(ImageUtil.isValidJpeg(validShort));
        assertTrue(ImageUtil.isValidJpeg(validJfif));
        assertFalse(ImageUtil.isValidJpeg(null));
        assertFalse(ImageUtil.isValidJpeg(new byte[] { (byte)0xFF }));
        assertFalse(ImageUtil.isValidJpeg(invalid));
    }

    @Test
    void tryReadFile_handlesExistingAndMissing() throws IOException {
        byte[] content = new byte[] {1,2,3,4,5};
        Path tmp = Files.createTempFile("imgutil", ".bin");
        Files.write(tmp, content);
        try {
            assertArrayEquals(content, ImageUtil.tryReadFile(tmp));
        } finally {
            Files.deleteIfExists(tmp);
        }

        assertNull(ImageUtil.tryReadFile(null));
        assertNull(ImageUtil.tryReadFile(Path.of("/path/that/does/not/exist/abc.bin")));
    }

    @Test
    void readResource_loadsBundledPng() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        byte[] data = ImageUtil.readResource(cl, "/slideshow-images/img.png");
        assertNotNull(data, "Should load packaged resource");
        assertTrue(ImageUtil.isValidPng(data), "Resource should be a PNG");
    }

    @Test
    void toDataUri_roundtrip() {
        byte[] bytes = new byte[] {9,8,7,6,5,4,3,2,1};
        String uri = ImageUtil.toDataUri(bytes, ImageUtil.PNG_MIME);
        assertTrue(uri.startsWith("data:" + ImageUtil.PNG_MIME + ";base64,") );
        String b64 = uri.substring(("data:" + ImageUtil.PNG_MIME + ";base64,").length());
        byte[] decoded = Base64.getDecoder().decode(b64);
        assertArrayEquals(bytes, decoded);
    }
}
