package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.tools.ColorTools;
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

public class LogFileManager {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(10_000);
    private final ExecutorService logWriter = Executors.newSingleThreadExecutor();
    private BufferedWriter writer;
    private long dropCount = 0;

    private File currentLogFile;

    public void init() {
        ensureLogDirectory();
        startNewSessionLogFile();
        startAsyncWriter();
    }

    /**
     * Writes a log message to the file asynchronously.
     * @param message The log message to write.
     */
    public void writeLogMessage(String message) {
        if (!logQueue.offer(message)) {
            dropCount++;
            LoggerManager.internalError("Dropped Messages (" + dropCount + "). I/O can't keep up! You should disable log files!");
        }
    }

    /**
     * Ensures the log directory exists.
     */
    private void ensureLogDirectory() {
        String logDirectory = LoggerManager.getSettings().getLogDirectory();
        try {
            Path logDirectoryPath = Path.of(logDirectory);
            Files.createDirectories(logDirectoryPath);
        } catch (IOException e) {
            LoggerManager.internalError("Failed to create log directory: '" + logDirectory + "'");
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
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            LoggerManager.internalError("Failed to write log message to file", e);
        }
    }

    /**
     * Rotates logs based on the configured limitation mode.
     */
    private void rotateLogsIfNeeded() {
        try {
            if (currentLogFile == null || !currentLogFile.exists()) {
                writer.close();
                startNewSessionLogFile(); // Start a new session log file if none exists
            }

            int limitationMode = LoggerManager.getSettings().getLogFileLimit();

            if (limitationMode > -1) {
                enforceFileLimit();
            }
        } catch (Exception e) {
            LoggerManager.internalError("Failed to rotate log files", e);
        }
    }

    /**
     * Creates a new log file for each session.
     */
    private void startNewSessionLogFile() {
        try {
            if (LoggerManager.getSettings().isCompressOldLogFiles()) {
                for (File f : getSortedLogFiles()) {
                    if (!f.getName().endsWith(".gz")) compressLogFile(f);
                }
            }

            String sessionId = generateSessionId();
            String logFileNamePattern = LoggerManager.getSettings().getLogFileNamePattern();

            // Replace {sessionId} placeholder with the generated session ID
            String newLogFileName = logFileNamePattern.replace("{date}", sessionId) + ".log";
            currentLogFile = Path.of(LoggerManager.getSettings().getLogDirectory()).resolve(newLogFileName).toFile();
            writer = new BufferedWriter(new FileWriter(currentLogFile));
        } catch (Exception e) {
            LoggerManager.internalError("Failed to start new session log file: '" + currentLogFile + "'", e);
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
        while (logFiles.size() >= LoggerManager.getSettings().getLogFileLimit()) {
            deleteFile(logFiles.removeFirst());
        }
    }

    /**
     * Retrieves sorted list of log files.
     */
    private @NotNull List<File> getSortedLogFiles() {
        String logDirectory = LoggerManager.getSettings().getLogDirectory();
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

    /**
     * Deletes the specified file.
     * @param f the file to delete.
     */
    private void deleteFile(@NotNull File f) {
        // If no compression is enabled, delete the file
        if (!f.delete()) {
            LoggerManager.internalError("Unable to delete file: '" + f.getAbsolutePath() + "'");
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
            deleteFile(file);
        } catch (IOException e) {
            LoggerManager.internalError("Failed to compress log file: '" + file.getAbsolutePath() + "'", e);
        }

        // After compression, check if the compressed file exists and get its size
        File compressedFile = new File(file.getPath() + ".gz");

        // If the compressed file was successfully created and exists, calculate its size
        if (compressedFile.exists()) {
            long compressedFileSize = compressedFile.length();

            // If the compressed file is still too large, delete it
            if (compressedFileSize > LoggerManager.getSettings().getCompressedFileSizeLimit() * 1024L) {
                deleteFile(compressedFile);
            }
        }
    }

    /**
     * Stops the log writer gracefully.
     */
    public void shutdown() {
        try {
            writer.close();
        } catch (IOException e) {
            LoggerManager.internalError("Failed to close log file writer", e);
        }
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
