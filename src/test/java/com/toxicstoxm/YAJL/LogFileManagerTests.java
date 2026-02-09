package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import org.junit.jupiter.api.Test;

public class LogFileManagerTests {

    @Test
    void emptyTest() {

    }

    @Test
    void test() {
        LoggerManager.configure()
                .enableLogFiles(true)
                .logDirectory("/home/dominik/Downloads/testLogDir")
                .logFileLimit(3)
                .compressOldLogFiles(true)
                .done();

        Logger logger = LoggerManager.getLogger(getClass());

        logger.info("Hello World!");

        LoggerManager.configure()
                .enableLogFiles(false)
                .done();

        LoggerManager.configure()
                .enableLogFiles(true)
                .done();

        logger.info("Hello World! 2");
    }

    /*private Path tempDir;

    private void setupLogger(int fileLimit) throws IOException {
        tempDir = Files.createTempDirectory("yajl-logtest-");

        LoggerManager.resetSettings();
        LoggerManager.configure()
                .enableLogFiles(true)
                .logDirectory(tempDir.toString())
                .logFileLimit(fileLimit)
                .done();
    }

    private List<Path> listLogFiles() throws IOException {
        try (var stream = Files.list(tempDir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".log"))
                    .toList();
        }
    }

    @AfterEach
    void cleanup() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
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

    @Test
    void testLogFileIsCreatedAndWritten() throws Exception {
        setupLogger(10);

        Logger logger = LoggerManager.getLogger(getClass());
        logger.info("Hello World");

        LoggerManager.getLogFileManager().shutdown();

        List<Path> logs = listLogFiles();
        assertEquals(1, logs.size());

        String content = Files.readString(logs.getFirst());
        assertTrue(content.contains("Hello World"));
    }

    @Test
    void testAnsiIsStrippedBeforeWriting() throws Exception {
        setupLogger(10);

        Logger logger = LoggerManager.getLogger(getClass());
        logger.info(
                ColorTools.toAnsi(java.awt.Color.RED)
                        + "RED_TEXT"
                        + ColorTools.ANSI_RESET
        );

        LoggerManager.getLogFileManager().shutdown();

        String content = Files.readString(listLogFiles().getFirst());

        assertFalse(content.contains("\u001B"), "ANSI escape code found in log file");
        assertTrue(content.contains("RED_TEXT"));
    }

    @Test
    void testRotationDeletesOldFiles() throws Exception {
        setupLogger(2);

        Logger logger = LoggerManager.getLogger(getClass());

        logger.info("Message 1");
        logger.info("Message 2");
        logger.info("Message 3");

        LoggerManager.getLogFileManager().shutdown();

        assertTrue(listLogFiles().size() <= 2);
    }

    @Test
    void testShutdownFlushesQueueAndClosesCleanly() throws Exception {
        setupLogger(10);

        Logger logger = LoggerManager.getLogger(getClass());
        logger.info("FlushTest");

        LoggerManager.getLogFileManager().shutdown();

        String content = Files.readString(listLogFiles().getFirst());
        assertTrue(content.contains("FlushTest"));
    }*/
}
