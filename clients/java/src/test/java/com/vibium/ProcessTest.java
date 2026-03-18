package com.vibium;

import com.vibium.internal.BinaryResolver;
import com.vibium.internal.PlatformDetector;
import com.vibium.types.StartOptions;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Binary discovery and process lifecycle tests.
 */
class ProcessTest {

    @Test
    void testPlatformDetection() {
        String platform = PlatformDetector.detect();
        assertNotNull(platform);
        // Should be one of the supported platforms
        assertTrue(
            platform.startsWith("darwin-") || platform.startsWith("linux-") || platform.startsWith("windows-"),
            "Unexpected platform: " + platform
        );
    }

    @Test
    void testBinaryResolve() {
        // Should find the binary via VIBIUM_BIN_PATH or PATH
        String path = BinaryResolver.resolve();
        assertNotNull(path);
        assertTrue(path.contains("vibium"));
    }

    @Test
    void testStartStop() {
        Browser browser = Vibium.start(new StartOptions().headless(true));
        assertNotNull(browser);

        Page page = browser.page();
        assertNotNull(page);

        browser.stop();
        // After stop, further calls should fail
    }

    @Test
    void testHeadlessMode() {
        Browser browser = Vibium.start(new StartOptions().headless(true));
        Page page = browser.page();
        page.go("about:blank");
        assertNotNull(page.url());
        browser.stop();
    }
}
