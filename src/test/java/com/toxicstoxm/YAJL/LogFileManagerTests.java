package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LogFileManagerTests {

    private Path tempDir;

    /* ---------------- setup / teardown ---------------- */

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("yajl-logtest-");

        LoggerManager.resetSettings();
        LoggerManager.configure()
                .enableLogFiles(true)
                .logDirectory(tempDir.toString())
                .compressOldLogFiles(true)
                .compressedFileSizeLimit(1024 * 1024) // large enough not to delete
                .logFileLimit(10)
                .done();
    }

    @AfterEach
    void cleanup() throws IOException {
        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    /* ---------------- helpers ---------------- */

    private @NonNull @Unmodifiable List<Path> listLogFiles() throws IOException {
        try (var stream = Files.list(tempDir)) {
            return stream
                    .filter(p ->
                            p.getFileName().toString().endsWith(".log") ||
                                    p.getFileName().toString().endsWith(".log.gz")
                    )
                    .sorted()
                    .toList();
        }
    }

    private void forceRotation() {
        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        LoggerManager.configure()
                .enableLogFiles(true)
                .done();
    }

    /* ---------------- core behavior ---------------- */

    @Test
    void logFileIsCreatedAndWritten() throws Exception {
        Logger logger = LoggerManager.getLogger(getClass());
        logger.info("Hello World");

        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        List<Path> logs = listLogFiles();
        assertEquals(1, logs.size());

        String content = Files.readString(logs.getFirst());
        assertTrue(content.contains("Hello World"));
    }

    @Test
    void oldLogFilesAreCompressedOnRotation() throws Exception {
        Logger logger = LoggerManager.getLogger(getClass());

        logger.info("First");
        forceRotation();

        logger.info("Second");
        forceRotation();

        logger.info("Third");

        LoggerManager.configure()
                .enableLogFiles(false)
                .done();
        List<Path> logs = listLogFiles();
        assertEquals(3, logs.size());

        assertEquals(2,
                logs.stream().filter(p -> p.toString().endsWith(".gz")).count());
        assertEquals(1,
                logs.stream().filter(p -> p.toString().endsWith(".log")).count());
    }

    @Test
    void fileLimitCountsLogAndGzipFiles() throws Exception {
        LoggerManager.configure()
                .logFileLimit(2)
                .done();

        Logger logger = LoggerManager.getLogger(getClass());

        logger.info("1");
        forceRotation();

        logger.info("2");
        forceRotation();

        logger.info("3");

        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        Thread.sleep(1000); // Let filesystem settle
        List<Path> logs = listLogFiles();
        assertEquals(2, logs.size());
    }

    /* ---------------- edge cases ---------------- */

    @Test
    void currentLogFileIsNeverDeletedByLimit() throws Exception {
        LoggerManager.configure()
                .logFileLimit(1)
                .done();

        Logger logger = LoggerManager.getLogger(getClass());

        logger.info("Old");
        forceRotation();

        logger.info("Current");

        Thread.sleep(1000); // Let filesystem settle
        List<Path> logs = listLogFiles();
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().toString().endsWith(".log"));
    }

    @Test
    void shutdownFlushesAsyncQueue() throws Exception {
        Logger logger = LoggerManager.getLogger(getClass());

        for (int i = 0; i < 500; i++) {
            logger.info("msg-" + i);
        }

        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        String content = Files.readString(listLogFiles().getFirst());
        assertTrue(content.contains("msg-0"));
        assertTrue(content.contains("msg-499"));
    }

    @Test
    void compressionFailureDoesNotDeleteOriginalFile() throws Exception {
        LoggerManager.configure()
                .compressedFileSizeLimit(0) // force deletion
                .done();

        Logger logger = LoggerManager.getLogger(getClass());
        logger.info("This is a large enough message to exceed limit");
        forceRotation();

        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        List<Path> logs = listLogFiles();
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().toString().endsWith(".log"));
    }

    @Test
    void disablingLogFilesMultipleTimesIsIdempotent() {
        assertDoesNotThrow(() -> {
            LoggerManager.configure().enableLogFiles(false).done();
            LoggerManager.configure().enableLogFiles(false).done();
            LoggerManager.configure().enableLogFiles(false).done();
        });
    }
}
