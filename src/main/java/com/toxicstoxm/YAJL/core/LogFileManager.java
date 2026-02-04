package com.toxicstoxm.YAJL.core;

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
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class LogFileManager {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(10_000);

    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService maintenanceExecutor = Executors.newSingleThreadExecutor();

    private BufferedWriter writer;
    private final AtomicLong dropCount = new AtomicLong(0);

    private File currentLogFile;
    private String currentLogCanonicalPath;

    public void init() {
        ensureLogDirectory();
        startNewSessionLogFile();
        enforceFileLimit();
        startAsyncWriter();
    }

    /**
     * Writes a log message to the file asynchronously.
     * @param message The log message to write.
     */
    public void writeLogMessage(String message) {
        if (!logQueue.offer(message)) {
            if (dropCount.incrementAndGet() % 1000 == 0) {
                LoggerManager.internalLog("Dropped messages: " + dropCount.get());
            }
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
            LoggerManager.internalLog("Failed to create log directory: '" + logDirectory + "'");
        }
    }

    /**
     * Starts the async log writer thread.
     */
    private void startAsyncWriter() {
        writeExecutor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    writeToFile(logQueue.take());
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    private int pendingWrites = 0;

    /**
     * Handles writing log messages to a file.
     */
    private void writeToFile(String message) {
        if (writer == null) {
            LoggerManager.internalLog("Log writer unavailable, dropping message");
            return;
        }

        try {
            if (currentLogFile == null || !currentLogFile.exists()) {
                if (writer != null) {
                    try {
                        writer.flush();
                    } finally {
                        writer.close();
                    }
                }
                startNewSessionLogFile();
                enforceFileLimit();
            }

            writer.write(message);
            writer.newLine();
            if (++pendingWrites >= 128) {
                writer.flush();
                pendingWrites = 0;
            }
        } catch (IOException e) {
            LoggerManager.internalLog("Failed to write log message to file", e);
        }
    }

    /**
     * Creates a new log file for each session.
     */
    private void startNewSessionLogFile() {
        try {
            if (LoggerManager.getSettings().isCompressOldLogFiles()) {
                for (File f : getSortedLogFiles()) {
                    if (f.getName().endsWith(".log")
                            && !new File(f.getPath() + ".gz").exists()
                            && !new File(f.getPath() + ".compressing").exists()) {
                        scheduleCompression(f);
                    }
                }
            }

            String sessionId = generateSessionId();
            String logFileNamePattern = LoggerManager.getSettings().getLogFileNamePattern();

            // Replace {sessionId} placeholder with the generated session ID
            String newLogFileName = logFileNamePattern.replace("{date}", sessionId) + ".log";
            Path newLogFile = Path.of(LoggerManager.getSettings().getLogDirectory()).resolve(newLogFileName);
            currentLogFile = newLogFile.toFile();
            currentLogCanonicalPath = currentLogFile.getCanonicalPath();
            writer = Files.newBufferedWriter(newLogFile, CREATE, APPEND);
            pendingWrites = 0;
        } catch (Exception e) {
            LoggerManager.internalLog("Failed to start new session log file: '" + currentLogFile + "'", e);
        }
    }

    private void scheduleCompression(@NotNull File file) {
        File tmp = new File(file.getPath() + ".compressing");

        if (!file.renameTo(tmp)) {
            LoggerManager.internalLog("Failed to rename file '" + file.getAbsolutePath() + "' to '" + tmp + "'");
            return;
        }

        maintenanceExecutor.execute(() -> compressLogFile(file, tmp));
    }


    /**
     * Generates a unique session ID based on the current timestamp.
     * @return A unique session ID.
     */
    private @NotNull String generateSessionId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS"));
    }

    /**
     * Deletes old log files if total file count exceeds limit.
     */
    private void enforceFileLimit() {
        if (LoggerManager.getSettings().getLogFileLimit() > -1) {
            List<File> logFiles = getSortedLogFiles();
            while (logFiles.size() >= LoggerManager.getSettings().getLogFileLimit()) {
                deleteFile(logFiles.removeFirst());
            }
        }
    }

    /**
     * Retrieves sorted list of log files.
     */
    private @NotNull List<File> getSortedLogFiles() {
        String logDirectory = LoggerManager.getSettings().getLogDirectory();
        Path logDirectoryPath = Path.of(logDirectory);

        // Retrieve all log files from the log directory
        File[] files = logDirectoryPath.toFile().listFiles((file, name) -> {
            try {
                return (name.endsWith(".log") || name.endsWith(".log.gz"))
                        && (currentLogFile == null || !file.getCanonicalPath().equals(currentLogCanonicalPath));
            } catch (IOException e) {
                LoggerManager.internalLog("Failed to get canonical path for file: '" + file.getAbsolutePath() + "'", e);
                return false;
            }
        });
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
        if (!f.delete()) {
            LoggerManager.internalLog("Unable to delete file: '" + f.getAbsolutePath() + "'");
        }
    }

    /**
     * Compresses a log file to a GZ format.
     */
    private void compressLogFile(@NotNull File file, @NotNull File tmp) {
        File compressedFile = new File(file.getPath() + ".gz");

        try (FileInputStream fis = new FileInputStream(tmp);
             FileOutputStream fos = new FileOutputStream(compressedFile);
             GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzipOut.write(buffer, 0, length);
            }
            gzipOut.finish();
            deleteFile(tmp);
        } catch (IOException e) {
            LoggerManager.internalLog("Failed to compress log file: '" + file.getAbsolutePath() + "'", e);
        }

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
            writeExecutor.shutdown();
            try {
                if (!writeExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    writeExecutor.shutdownNow();
                    if (!writeExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Failed to terminate writeExecutor!");
                    }
                }
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                currentLogFile = null;
            }
        } catch (InterruptedException | IOException e) {
            LoggerManager.internalLog("Failed to stop log message writer!", e);
        }

        try {
            maintenanceExecutor.shutdown();
            if (!maintenanceExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LoggerManager.internalLog("Failed to stop maintenance worker", e);
        }
    }
}
