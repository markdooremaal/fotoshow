package com.example;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.util.EnvUtil.defaultIfBlank;
import static com.example.util.EnvUtil.parsePositiveInt;
import static com.example.util.ImageUtil.JPEG_MIME;
import static com.example.util.ImageUtil.PNG_MIME;
import static com.example.util.ImageUtil.TRANSPARENT_PX_DATA_URI;
import static com.example.util.ImageUtil.isValidJpeg;
import static com.example.util.ImageUtil.isValidPng;
import static com.example.util.ImageUtil.readResource;
import static com.example.util.ImageUtil.toDataUri;
import static com.example.util.ImageUtil.tryReadFile;
import static java.util.Objects.requireNonNull;

@Path("/")
public class SlideshowResource {

    private static final String CLASSPATH_IMAGES_DIR = "/slideshow-images/";
    private static final String ENV_EXTERNAL_DIR = "SLIDESHOW_EXTERNAL_DIR";

    private final Template slideshow;

    public SlideshowResource(Template slideshow) {
        this.slideshow = requireNonNull(slideshow, "slideshow template is required");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance show() {
        Map<String, String> env = System.getenv();

        int imageCount = parsePositiveInt(env.get("SLIDESHOW_IMAGE_COUNT"), 5, 1, 100);
        String backdropColor = defaultIfBlank(env.get("SLIDESHOW_BACKDROP_COLOR"), "#111111");
        int refreshMs = parsePositiveInt(env.get("SLIDESHOW_REFRESH_MS"), 3000, 250, 60000);

        String externalDir = defaultIfBlank(env.get(ENV_EXTERNAL_DIR), null);
        List<String> images = loadImagesWithFallback(externalDir, imageCount);
        if (images.isEmpty()) {
            images = List.of(TRANSPARENT_PX_DATA_URI);
        }

        TemplateInstance inst = slideshow.instance();
        inst = inst.data("images", images)
                .data("backdropColor", backdropColor)
                .data("refreshMs", refreshMs);
        return inst;
    }

    private static List<String> loadImagesWithFallback(String externalDir, int count) {
        List<String> result = new ArrayList<>(count);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        for (int i = 0; i < count; i++) {
            String baseName = (i == 0) ? "img" : ("img_" + i);

            byte[] bytes = null;
            String mime = PNG_MIME;

            // 1) Try external directory (png, jpg, jpeg) if configured
            if (externalDir != null) {
                byte[] candidate = tryReadFile(Paths.get(externalDir, baseName + ".png"));
                if (isValidPng(candidate)) {
                    bytes = candidate;
                    mime = PNG_MIME;
                }
                if (bytes == null) {
                    candidate = tryReadFile(Paths.get(externalDir, baseName + ".jpg"));
                    if (isValidJpeg(candidate)) {
                        bytes = candidate;
                        mime = JPEG_MIME;
                    }
                }
                if (bytes == null) {
                    candidate = tryReadFile(Paths.get(externalDir, baseName + ".jpeg"));
                    if (isValidJpeg(candidate)) {
                        bytes = candidate;
                        mime = JPEG_MIME;
                    }
                }
            }

            // 2) Fallback to classpath resource (png only)
            if (bytes == null) {
                String path = CLASSPATH_IMAGES_DIR + baseName + ".png";
                byte[] candidate = readResource(cl, path);
                if (isValidPng(candidate)) {
                    bytes = candidate;
                    mime = PNG_MIME;
                }
            }

            if (bytes != null && bytes.length > 0) {
                result.add(toDataUri(bytes, mime));
            }
        }
        return result;
    }
}
