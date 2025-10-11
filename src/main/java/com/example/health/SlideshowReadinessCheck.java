package com.example.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Readiness check for the slideshow: verifies that at least one image is available
 * either from the configured external directory or from the classpath fallback.
 */
@Readiness
@ApplicationScoped
public class SlideshowReadinessCheck implements HealthCheck {

    private static final String ENV_EXTERNAL_DIR = "SLIDESHOW_EXTERNAL_DIR";
    private static final String CLASSPATH_IMG = "slideshow-images/img.png"; // classpath lookup path (no leading slash)

    @Override
    public HealthCheckResponse call() {
        Map<String, String> env = System.getenv();
        String externalDir = trimToNull(env.get(ENV_EXTERNAL_DIR));

        boolean externalOk = hasAnyExternalImage(externalDir);
        boolean classpathOk = hasClasspathImage();
        boolean up = externalOk || classpathOk;

        HealthCheckResponseBuilder b = HealthCheckResponse.named("slideshow-readiness");
        if (up) {
            b.up();
        } else {
            b.down();
        }
        b.withData("externalDir", externalDir == null ? "(none)" : externalDir)
         .withData("externalHasImg", externalOk)
         .withData("classpathHasImg", classpathOk);
        return b.build();
    }

    private static boolean hasAnyExternalImage(String externalDir) {
        if (externalDir == null) return false;
        // Check common names for the first slide only to keep it fast
        return exists(Path.of(externalDir, "img.png"))
                || exists(Path.of(externalDir, "img.jpg"))
                || exists(Path.of(externalDir, "img.jpeg"));
    }

    private static boolean hasClasspathImage() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(CLASSPATH_IMG) != null;
    }

    private static boolean exists(Path p) {
        try {
            return Files.isRegularFile(p);
        } catch (Exception e) {
            return false;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
