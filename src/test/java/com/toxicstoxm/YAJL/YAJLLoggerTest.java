package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.areas.LogAreaBundle;
import com.toxicstoxm.YAJL.areas.YAJLLogArea;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class YAJLLoggerTest {

    public static String tmpDir = System.getenv("XDG_CACHE_HOME") == null ?
            System.getProperty("java.io.tmpdir") + "/LEDSuite/" :
            System.getenv("XDG_CACHE_HOME") + "/";

    private static class TestBundle implements LogAreaBundle {
        public static class TestArea extends YAJLLogArea {
            public TestArea() {
                super(new Color(140, 140, 140));
            }
        }
    }

    private final YAJLLogger testLogger;
    private final ByteArrayOutputStream testOutputStream;
    private final String testProjectDir = tmpDir;
    private final String testMessage = "YAJL is cool :)! \n by ToxicStoxm \n ._. ._. ._. \n something really funny";
    private final List<String> expectedMessages = List.of("YAJL is cool :)!", "by ToxicStoxm", "._. ._. ._.", "something really funny");
    private final URL testConfigPath = getClass().getClassLoader().getResource("yajl-config.test.yaml");
    private final String testAreaName = Arrays.stream(TestBundle.TestArea.class.getName().split("\\$")).toList().getLast();

    private final String fatal = "FATAL";
    private final String error = "ERROR";
    private final String warn = "WARN";
    private final String info = "INFO";
    private final String debug = "DEBUG";
    private final String verbose = "VERBOSE";
    private final String stacktrace = "STACKTRACE";

    public YAJLLoggerTest() {
        testOutputStream = new ByteArrayOutputStream();
        testLogger = YAJLLogger.builder()
                .build(
                        testProjectDir,
                        new PrintStream(testOutputStream),
                        new TestBundle.TestArea(),
                        true
                );
    }
    
    private boolean containsAll(String base, Collection<String> elements) {
        AtomicBoolean result = new AtomicBoolean(true);
        elements.forEach(string -> {
            if (!base.contains(string)) result.set(false);
        });
        return result.get();
    }

    // construction functions

    @Test
    void builder() {
        assertEquals(YAJLLogger.class, YAJLLogger.builder().getClass());
    }

    @Test
    void setSettingsManager() {
        YAJSISettingsManager settingsManager = YAJSISettingsManager.builder()
                .build(
                        Collections.singleton(new YAJSISettingsManager.YAMLConfig(
                                new YAJSISettingsManager.ConfigFile(
                                        testProjectDir,
                                        testConfigPath
                                ),
                                YAJLSettingsBundle.class
                        ))
                );
        YAJLLogger logger = YAJLLogger.builder().setSettingsManager(settingsManager);
        assertEquals(settingsManager, logger.settingsManager);
        assertThrows(RuntimeException.class, () -> logger.log(testMessage));
    }

    @Test
    void setEnableAreaWildcard() {
        testLogger.registerLogAreaBundle(new TestBundle());
        testLogger.info(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(testAreaName));
    }

    @Test
    void build() {
        assertDoesNotThrow(() -> testLogger.log(testMessage));
    }

    @Test
    void getDefaultLogArea() {
        assertTrue(testLogger.getDefaultLogArea() instanceof TestBundle.TestArea);
    }

    // logging functions

    @Test
    void fatal() {
        testLogger.fatal(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(fatal));
    }

    @Test
    void error() {
        testLogger.error(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(error));
    }

    @Test
    void warn() {
        testLogger.warn(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(warn));
    }

    @Test
    void info() {
        testLogger.info(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(info));
    }

    @Test
    void debug() {
        testLogger.debug(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(debug));
    }

    @Test
    void verbose() {
        testLogger.verbose(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(verbose));
    }

    @Test
    void stacktrace() {
        testLogger.stacktrace(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(stacktrace));
    }

    @Test
    void log() {
        testLogger.log(testMessage, new YAJLLogLevels.Info(), new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(info) && result.contains(testAreaName));
    }

    @Test
    void testFatal() {
        testLogger.fatal(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(fatal) && result.contains(testAreaName));
    }

    @Test
    void testError() {
        testLogger.error(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(error) && result.contains(testAreaName));
    }

    @Test
    void testWarn() {
        testLogger.warn(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(warn) && result.contains(testAreaName));
    }

    @Test
    void testInfo() {
        testLogger.info(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(info) && result.contains(testAreaName));
    }

    @Test
    void testDebug() {
        testLogger.debug(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(debug) && result.contains(testAreaName));
    }

    @Test
    void testVerbose() {
        testLogger.verbose(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(verbose) && result.contains(testAreaName));
    }

    @Test
    void testStacktrace() {
        testLogger.stacktrace(testMessage, new TestBundle.TestArea());
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages) && result.contains(stacktrace) && result.contains(testAreaName));
    }

    @Test
    void testLog() {
        testLogger.log(testMessage);
        String result = testOutputStream.toString();
        assertTrue(containsAll(result, expectedMessages));
    }
}