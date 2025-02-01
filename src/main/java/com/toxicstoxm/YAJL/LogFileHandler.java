package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.tools.ColorTools;
import com.toxicstoxm.YAJSI.api.settings.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

public class LogFileHandler {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final ExecutorService logWriter = Executors.newSingleThreadExecutor();
    private boolean init = false;

    private File currentLogFile;

    public LogFileHandler() {
        if (YAJLManager.getInstance().config.getLogFileConfig().isEnable()) setEnabled(true);
    }

    /**
     * Initializes / de-initializes the log manager.
     * @param enable
     */
    public void setEnabled(boolean enable) {
        if (enable) {
            if (init) return;
            ensureLogDirectory();
            startNewSessionLogFile();
            startAsyncWriter();
            init = true;
        } else {
            if (!init) return;
            shutdown();
            init = false;
        }
    }

    /**
     * Writes a log message to the file asynchronously.
     * @param message The log message to write.
     */
    public void writeLogMessage(String message) {
        if (YAJLManager.getInstance().config.getLogFileConfig().isEnable()) logQueue.offer(message);
    }

    private @NotNull String getLogDirectory() {
        String configDir = SettingsManager.getInstance().getConfigDirectory();
        return configDir + "/"
                + YAJLManager.getInstance().config.getLogFileConfig().getLogDirectory();
    }

    /**
     * Ensures the log directory exists.
     */
    private void ensureLogDirectory() {
        String logDirectory = getLogDirectory();
        try {
            Path logDirectoryPath = Path.of(logDirectory);
            Files.createDirectories(logDirectoryPath);
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.out.println("[YAJL] Initialized log file handler with directory: " + logDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log directory: " + logDirectory, e);
        }
    }

    /**
     * Starts the async log writer thread.
     */
    private void startAsyncWriter() {
        logWriter.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String logMessage = logQueue.take(); // Blocks until a message is available
                    writeToFile(ColorTools.stripAnsi(logMessage));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * Handles writing log messages to a file.
     */
    private synchronized void writeToFile(String message) {
        try {
            if (currentLogFile == null || !currentLogFile.exists()) {
                rotateLogsIfNeeded();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLogFile, true))) {
                writer.write(message);
                writer.newLine();
            }
        } catch (IOException e) {
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.err.println("[YAJL] Error writing log message: " + e.getMessage());
            }
        }
    }

    /**
     * Rotates logs based on the configured limitation mode.
     */
    private void rotateLogsIfNeeded() {
        try {
            if (currentLogFile == null || !currentLogFile.exists()) {
                startNewSessionLogFile(); // Start a new session log file if none exists
            }

            String limitationMode = YAJLManager.getInstance().config.getLogFileConfig().getLimitationMode();

            // Rotate the log files if the limitation mode is "files"
            if (limitationMode.equalsIgnoreCase("files")) {
                enforceFileLimit();
            }
        } catch (Exception e) {
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.err.println("[YAJL] Failed to rotate logs: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a new log file for each session.
     */
    private void startNewSessionLogFile() {
        try {
            if (YAJLManager.getInstance().config.getLogFileConfig().isCompressOldLogFiles()) {
                for (File f : getSortedLogFiles()) {
                    if (!f.getName().endsWith(".gz")) compressLogFile(f);
                }
            }

            String sessionId = generateSessionId();
            String logFileNamePattern = YAJLManager.getInstance().config.getLogFileConfig().getLogFileName();

            // Replace {sessionId} placeholder with the generated session ID
            String newLogFileName = logFileNamePattern.replace("{date}", sessionId) + ".log";
            currentLogFile = Path.of(getLogDirectory()).resolve(newLogFileName).toFile();

            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.out.println("[YAJL] Created new log file: " + currentLogFile.getName());
            }
        } catch (Exception e) {
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.err.println("[YAJL] Failed to start new session log file: " + e.getMessage());
            }
        }
    }

    /**
     * Generates a unique session ID based on the current timestamp.
     * @return A unique session ID.
     */
    private @NotNull String generateSessionId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }

    /**
     * Deletes old log files if total file count exceeds limit.
     */
    private void enforceFileLimit() {
        List<File> logFiles = getSortedLogFiles();
        while (logFiles.size() >= YAJLManager.getInstance().config.getLogFileConfig().getLimitationNumber()) {
            deleteFile(logFiles.removeFirst());
        }
    }

    /**
     * Retrieves sorted list of log files.
     */
    private @NotNull List<File> getSortedLogFiles() {
        String logDirectory = getLogDirectory();
        Path logDirectoryPath = Path.of(logDirectory);

        // Retrieve all files in the log directory that end with ".log"
        File[] files = logDirectoryPath.toFile().listFiles((_, name) -> name.endsWith(".log") || name.endsWith(".log.gz"));
        List<File> logFiles = new ArrayList<>();

        if (files != null) {
            logFiles.addAll(Arrays.asList(files));

            // Sort files by last modified time (oldest first)
            logFiles.sort(Comparator.comparingLong(File::lastModified));
        }

        return logFiles;
    }

    private void deleteFile(@NotNull File f) {
        // If no compression is enabled, delete the file
        if (f.delete()) {
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.out.println("[YAJL] Deleted log file: " + f.getName());
            }
        } else {
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.err.println("[YAJL] Failed to delete log file: " + f.getName() + "!");
            }
        }
    }

    /**
     * Compresses a log file to a GZ format.
     */
    private void compressLogFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(file.getPath() + ".gz");
             GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzipOut.write(buffer, 0, length);
            }
            gzipOut.finish();
            if (!file.delete() && !YAJLManager.getInstance().config.isMuteLogger()) {
                System.err.println("[YAJL] Failed to delete original file: " + file.getName() + ", after compressing!");
            }
        } catch (IOException e) {
            if (!YAJLManager.getInstance().config.isMuteLogger()) {
                System.err.println("[YAJL] Failed to compress log file: " + file.getName());
            }
        }

        // After compression, check if the compressed file exists and get its size
        File compressedFile = new File(file.getPath() + ".gz");

        // If the compressed file was successfully created and exists, calculate its size
        if (compressedFile.exists()) {
            long compressedFileSize = compressedFile.length();

            // If the compressed file is still too large, delete it
            if (compressedFileSize > YAJLManager.getInstance().config.getLogFileConfig().getLimitationNumber() * 1024L) {
                if (compressedFile.delete()) {
                    if (!YAJLManager.getInstance().config.isMuteLogger()) {
                        System.out.println("[YAJL] Deleted compressed log file: " + compressedFile.getName() + " because it exceeds the size limit.");
                    }
                } else {
                    if (!YAJLManager.getInstance().config.isMuteLogger()) {
                        System.err.println("[YAJL] Failed to delete compressed log file: " + compressedFile.getName() + "!");
                    }
                }
            }
        }
    }

    /**
     * Stops the log writer gracefully.
     */
    public void shutdown() {
        logWriter.shutdown();
        try {
            if (!logWriter.awaitTermination(3, TimeUnit.SECONDS)) {
                logWriter.shutdownNow();
            }
        } catch (InterruptedException e) {
            logWriter.shutdownNow();
        }
    }
}
