package com.vibium.internal;

import com.vibium.errors.VibiumNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Finds or extracts the vibium binary.
 *
 * Resolution order:
 * 1. VIBIUM_BIN_PATH environment variable
 * 2. PATH lookup
 * 3. Extract from JAR resources
 */
public final class BinaryResolver {

    private BinaryResolver() {}

    /**
     * Resolve the path to the vibium binary.
     */
    public static String resolve() {
        // 1. Environment variable
        String envPath = System.getenv("VIBIUM_BIN_PATH");
        if (envPath != null && !envPath.isEmpty()) {
            Path p = Paths.get(envPath);
            if (Files.isExecutable(p)) {
                return p.toAbsolutePath().toString();
            }
        }

        // 2. PATH lookup
        String pathResult = findOnPath();
        if (pathResult != null) {
            return pathResult;
        }

        // 3. Extract from JAR
        String extracted = extractFromJar();
        if (extracted != null) {
            return extracted;
        }

        throw new VibiumNotFoundException(
            "vibium binary not found. Install it via npm (npm install vibium), " +
            "set VIBIUM_BIN_PATH, or ensure it's on your PATH."
        );
    }

    private static String findOnPath() {
        String execName = PlatformDetector.executableName();
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return null;

        for (String dir : pathEnv.split(System.getProperty("path.separator"))) {
            Path candidate = Paths.get(dir, execName);
            if (Files.isExecutable(candidate)) {
                return candidate.toAbsolutePath().toString();
            }
        }
        return null;
    }

    private static String extractFromJar() {
        String resourceName = "natives/" + PlatformDetector.binaryName();
        InputStream stream = BinaryResolver.class.getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            return null;
        }

        try {
            // Read version for cache directory
            String version = readVersion();
            Path extractDir = Paths.get(System.getProperty("java.io.tmpdir"), "vibium-" + version);
            Files.createDirectories(extractDir);

            Path target = extractDir.resolve(PlatformDetector.executableName());

            // Only extract if not already present
            if (!Files.exists(target)) {
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
                // Set executable permission on Unix
                if (!System.getProperty("os.name", "").toLowerCase().contains("windows")) {
                    target.toFile().setExecutable(true);
                }
            }
            stream.close();
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            try { stream.close(); } catch (IOException ignored) {}
            return null;
        }
    }

    private static String readVersion() {
        try (InputStream is = BinaryResolver.class.getClassLoader().getResourceAsStream("vibium-version.txt")) {
            if (is != null) {
                return new String(readAllBytes(is)).trim();
            }
        } catch (IOException ignored) {}
        return "unknown";
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        while ((len = is.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        return out.toByteArray();
    }
}
