package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.areas.LogAreaBundle;
import com.toxicstoxm.YAJL.areas.YAJLLogArea;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.*;
import java.util.Collections;

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
    private final ByteArrayOutputStream testStream;
    private final String test = "Test";
    private final String projectDir = tmpDir;

    public YAJLLoggerTest() {
        testStream = new ByteArrayOutputStream();
        testLogger = YAJLLogger.builder()
                .build(
                        projectDir,
                        new PrintStream(testStream),
                        new TestBundle.TestArea(),
                        true
                );
    }

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
                                        projectDir,
                                        getClass().getClassLoader().getResource("yajl-config.yaml")
                                ),
                                YAJLSettingsBundle.class
                        ))
                );
        YAJLLogger logger = YAJLLogger.builder().setSettingsManager(settingsManager);
        assertEquals(settingsManager, logger.settingsManager);
        assertThrows(RuntimeException.class, () -> logger.log("Test"));
    }

    @Test
    void setEnableAreaWildcard() {
        testLogger.registerLogAreaBundle(new TestBundle());
        testLogger.info("", new TestBundle.TestArea());
        assertTrue(testStream.toString().contains("TestArea"));
    }

    @Test
    void build() {
        assertDoesNotThrow(() -> testLogger.log("Test"));
    }

    @Test
    void getDefaultLogArea() {
        assertTrue(testLogger.getDefaultLogArea() instanceof TestBundle.TestArea);
    }

    @Test
    void fatal() {
        testLogger.fatal(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("FATAL"));
    }

    @Test
    void error() {
        testLogger.error(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("ERROR"));
    }

    @Test
    void warn() {
        testLogger.warn(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("WARN"));
    }

    @Test
    void info() {
        testLogger.info(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("INFO"));
    }

    @Test
    void debug() {
        testLogger.debug(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("DEBUG"));
    }

    @Test
    void verbose() {
        testLogger.verbose(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("VERBOSE"));
    }

    @Test
    void stacktrace() {
        testLogger.stacktrace(test);
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("STACKTRACE"));
    }

    @Test
    void log() {
        testLogger.log(test, new YAJLLogLevels.Info(), new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("INFO") && result.contains("TestArea"));
    }

    @Test
    void testFatal() {
        testLogger.fatal(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("FATAL") && result.contains("TestArea"));
    }

    @Test
    void testError() {
        testLogger.error(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("ERROR") && result.contains("TestArea"));
    }

    @Test
    void testWarn() {
        testLogger.warn(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("WARN") && result.contains("TestArea"));
    }

    @Test
    void testInfo() {
        testLogger.info(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("INFO") && result.contains("TestArea"));
    }

    @Test
    void testDebug() {
        testLogger.debug(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("DEBUG") && result.contains("TestArea"));
    }

    @Test
    void testVerbose() {
        testLogger.verbose(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("VERBOSE") && result.contains("TestArea"));
    }

    @Test
    void testStacktrace() {
        testLogger.stacktrace(test, new TestBundle.TestArea());
        String result = testStream.toString();
        assertTrue(result.contains(test) && result.contains("STACKTRACE") && result.contains("TestArea"));
    }

    @Test
    void testLog() {
        testLogger.log(test);
        String result = testStream.toString();
        assertTrue(result.contains(test));
    }
}