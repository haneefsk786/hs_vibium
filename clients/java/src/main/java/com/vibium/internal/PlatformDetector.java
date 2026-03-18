package com.vibium.internal;

/**
 * Detects the current OS and architecture, mapping to vibium binary names.
 */
public final class PlatformDetector {

    private PlatformDetector() {}

    /**
     * Returns the platform key, e.g. "darwin-arm64", "linux-amd64", "windows-amd64".
     */
    public static String detect() {
        String os = normalizeOS(System.getProperty("os.name", ""));
        String arch = normalizeArch(System.getProperty("os.arch", ""));
        return os + "-" + arch;
    }

    /**
     * Returns the binary filename for the current platform.
     */
    public static String binaryName() {
        String platform = detect();
        if (platform.startsWith("windows-")) {
            return "vibium-" + platform + ".exe";
        }
        return "vibium-" + platform;
    }

    /**
     * Returns just "vibium" or "vibium.exe" depending on platform.
     */
    public static String executableName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            return "vibium.exe";
        }
        return "vibium";
    }

    private static String normalizeOS(String osName) {
        String lower = osName.toLowerCase();
        if (lower.contains("mac") || lower.contains("darwin")) {
            return "darwin";
        } else if (lower.contains("linux")) {
            return "linux";
        } else if (lower.contains("windows")) {
            return "windows";
        }
        throw new UnsupportedOperationException("Unsupported OS: " + osName);
    }

    private static String normalizeArch(String archName) {
        String lower = archName.toLowerCase();
        if (lower.equals("amd64") || lower.equals("x86_64")) {
            return "amd64";
        } else if (lower.equals("aarch64") || lower.equals("arm64")) {
            return "arm64";
        }
        throw new UnsupportedOperationException("Unsupported architecture: " + archName);
    }
}
