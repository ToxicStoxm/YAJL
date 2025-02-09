package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJL.level.LogLevels;
import com.toxicstoxm.YAJL.placeholders.LogMessagePlaceholder;
import com.toxicstoxm.YAJL.placeholders.StringPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoggerTest {

    private Logger logger;

    @BeforeEach
    void setUp() {
        YAJLManager.configure(
                YAJLManagerConfig.builder()
                        .enableYAMLConfig(false)
                        .build()
        ).setMinimumLogLevel(-20)
                .setStackTraceLengthLimit(20);
        logger = Logger.builder()
                .logPrefix("TestPrefix")
                .logArea("TestArea")
                .build();
    }

    @Test
    void testAutoConfigureLogger() {
        Logger autoConfiguredLogger = Logger.autoConfigureLogger();
        assertNotNull(autoConfiguredLogger);
        assertNotNull(autoConfiguredLogger.toString());
    }

    @Test
    void testDebugLogging() {
        logger.debug("Debug message");
        // No assertion here, just ensuring no exception is thrown
    }

    @Test
    void testInfoLogging() {
        logger.info("Info message");
    }

    @Test
    void testWarnLogging() {
        logger.warn("Warning message");
    }

    @Test
    void testErrorLogging() {
        logger.error("Error message");
    }

    @Test
    void testFatalLogging() {
        logger.fatal("Fatal error message");
    }

    @Test
    void testLogMessageFormatting() {
        String message = "This is a log message with a placeholder: {}";
        String formattedMessage = logger.format(message, "TestObject");

        assertEquals("This is a log message with a placeholder: TestObject", formattedMessage);
    }

    @Test
    void testLogException() {
        Exception testException = new Exception("Test exception");
        logger.error("Error occurred", testException);
    }

    @Test
    void testProcessLogMessage() {
        Map<String, StringPlaceholder> args = Map.of(
                "level", LogLevels.DEBUG::getName,
                "message", () -> "Test message"
        );
        String processedMessage = logger.processLogMessage("{level}: {message}", args);
        assertEquals("DEBUG: Test message", processedMessage);
    }

    @Test
    public void APITest() {
        // Complex Placeholder Tests
        logger.log(LogLevels.DEBUG, "Int: {}, Double: {}, Char: {}, Boolean: {}, Null: {}",
                42, 3.14159, 'X', true, null);

        logger.info("Nested Array Test: {}", (LogMessagePlaceholder) () -> (Object) new int[][][]{{{1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}}, {{1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}}});

        logger.info("Highly Nested Object Test: {}", (LogMessagePlaceholder) this::createDeepNestedStructure);

        logger.log(LogLevels.INFO, "Array Test: {}", new int[]{1, 2, 3, 4, 5});
        logger.log(LogLevels.WARN, "Object Test: {}", new Object() {
            @Override
            public String toString() {
                return "I'm an anonymous object!";
            }
        });

        logger.error("Shit\n");
        logger.debug("Hello \nWorld!");

        // Exception Formatting Tests
        Exception complexException = new Exception("Top-Level Exception",
                new RuntimeException("Nested Exception", new NullPointerException("Deep Nested Exception")));
        logger.error("Exception Stacktrace Test: {}", complexException);

        // Nested Logging Calls
        logger.debug("This will log another log call: {}", logger.toString());

        // Edge Cases
        logger.log(LogLevels.FATAL, "Empty Placeholder Test: {} {}", "", "");
        logger.log(LogLevels.ERROR, "Same Placeholder Multiple Times: {} {} {}", "Repeat", "Repeat", "Repeat");
        logger.log(LogLevels.WARN, "Escaped Braces {{}} should not break formatting");

        // Placeholder Overflow Test (More placeholders than arguments)
        logger.log(LogLevels.INFO, "Extra placeholders: {} {} {}", "OnlyOneArg");

        // Placeholder Underflow Test (More arguments than placeholders)
        logger.log(LogLevels.DEBUG, "Not enough placeholders: {}", "Arg1", "Arg2", "Arg3");

        // Very Large Number of Arguments
        logger.log(LogLevels.VERBOSE, "Massive argument test: {} {} {} {} {} {} {} {} {} {}",
                1, "Two", 3.0, true, 'C', 6.78f, null, new int[]{9, 10, 11}, new Object() {
                    @Override
                    public String toString() {
                        return "AnonymousClass";
                    }
                }, "End");

        // Test Log Prefix and Area
        assertEquals("[Name='TestPrefix', ID='TestArea']", logger.toString());
    }

    private @NotNull Object createDeepNestedStructure() {
        Map<String, Object> level1 = new HashMap<>();
        List<Object> level2 = new ArrayList<>();
        Set<Object> level3 = new HashSet<>();
        int[][][] level4 = {{{1, 2}, {3, 4}}, {{5, 6}, {7, 8}}};  // Multi-dimensional array

        // Go deeper by nesting maps, lists, and sets recursively
        for (int i = 5; i <= 500; i++) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("Layer-" + i, level1.isEmpty() ? level4 : level1);
            level1 = newMap;
        }

        level3.add(level1);
        level2.add(level3);

        Map<String, Object> root = new HashMap<>();
        root.put("Root", level2);

        return root;
    }
}
