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

public final class LogFileManager {
    private enum State {
        STOPPED, STARTING, RUNNING, STOPPING
    }

    private final Object lifecycleLock = new Object();
    private volatile State state = State.STOPPED;

    private final BlockingQueue<String> logQueue =
            new LinkedBlockingQueue<>(10_000);

    private ExecutorService writeExecutor;
    private ExecutorService maintenanceExecutor;

    private volatile BufferedWriter writer;
    private volatile File currentLogFile;
    private volatile String currentLogFileCanonicalPath;

    private final AtomicLong dropCount = new AtomicLong();
    private int pendingWrites = 0;

    public void init() {
        synchronized (lifecycleLock) {
            if (state == State.RUNNING || state == State.STARTING) return;
            state = State.STARTING;

            writeExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "YAJL-LogWriter"));
            maintenanceExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "YAJL-LogMaintenance"));

            ensureLogDirectory();
            startNewSessionLogFile();

            // Now we are ready to process old files
            state = State.RUNNING;

            // Schedule compression AFTER maintenanceExecutor is ready and state is RUNNING
            if (LoggerManager.getSettings().isCompressOldLogFiles()) {
                scheduleCompressionForOldFiles();
            }

            enforceFileLimit();
            startAsyncWriter();
        }
    }

    public void writeLogMessage(String message) {
        if (state != State.RUNNING) {
            return;
        }

        if (!logQueue.offer(message)) {
            if (dropCount.incrementAndGet() % 1000 == 0) {
                LoggerManager.internalLog(
                        "Dropped log messages: " + dropCount.get()
                );
            }
        }
    }

    private void startAsyncWriter() {
        writeExecutor.execute(() -> {
            try {
                while (state == State.RUNNING || !logQueue.isEmpty()) {
                    String msg = logQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        writeToFile(msg);
                    }
                }
            } catch (InterruptedException ignored) {
            } finally {
                flushAndCloseWriter();
            }
        });
    }

    private void writeToFile(String message) {
        BufferedWriter w = writer;
        if (w == null) return;

        try {
            w.write(message);
            w.newLine();

            if (++pendingWrites >= 128) {
                w.flush();
                pendingWrites = 0;
            }
        } catch (IOException e) {
            LoggerManager.internalLog("Failed to write log message", e);
        }
    }

    public void shutdown() {
        synchronized (lifecycleLock) {
            if (state == State.STOPPED || state == State.STOPPING) {
                return;
            }
            state = State.STOPPING;
        }

        shutdownExecutor(writeExecutor);
        shutdownExecutor(maintenanceExecutor);

        synchronized (lifecycleLock) {
            writeExecutor = null;
            maintenanceExecutor = null;
            state = State.STOPPED;
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor == null) return;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void flushAndCloseWriter() {
        BufferedWriter w = writer;
        writer = null;

        if (w != null) {
            try {
                w.flush();
                w.close();

                // mark old file for compression if enabled
                if (LoggerManager.getSettings().isCompressOldLogFiles() && currentLogFile != null) {
                    File oldFile = currentLogFile;
                    File tmp = new File(oldFile.getPath() + ".compressing");
                    if (oldFile.renameTo(tmp)) {
                        submitCompression(tmp, new File(oldFile.getPath() + ".gz"));
                    }
                }

            } catch (IOException e) {
                LoggerManager.internalLog("Failed to close log writer", e);
            }
        }

        currentLogFile = null;
        currentLogFileCanonicalPath = null;
    }

    private void ensureLogDirectory() {
        try {
            Files.createDirectories(
                    Path.of(LoggerManager.getSettings().getLogDirectory())
            );
        } catch (IOException e) {
            LoggerManager.internalLog("Failed to create log directory", e);
        }
    }

    private void startNewSessionLogFile() {
        // compress any old files that are still .log
        if (LoggerManager.getSettings().isCompressOldLogFiles()) {
            scheduleCompressionForOldFiles(); // look at all files in dir
        }

        String name = LoggerManager.getSettings()
                .getLogFileNamePattern()
                .replace("{date}", generateSessionId()) + ".log";

        Path file = Path.of(LoggerManager.getSettings().getLogDirectory(), name);

        currentLogFile = file.toFile();

        try {
            currentLogFileCanonicalPath = currentLogFile.getCanonicalPath();
        } catch (IOException e) {
            LoggerManager.internalLog("Failed to get canonical path for current log file", e);
        }

        try {
            writer = Files.newBufferedWriter(file, CREATE, APPEND);
        } catch (IOException e) {
            LoggerManager.internalLog("Failed to create new buffered writer", e);
        }
        pendingWrites = 0;
    }

    private void scheduleCompressionForOldFiles() {
        List<File> files = getSortedLogFiles();

        for (File f : files) {
            File gz = new File(f.getPath() + ".gz");
            File tmp = new File(f.getPath() + ".compressing");

            if (gz.exists() || tmp.exists()) continue;

            if (!f.renameTo(tmp)) {
                LoggerManager.internalLog("Failed to mark file for compression: " + f);
                continue;
            }

            submitCompression(tmp, gz);
        }
    }

    private void submitCompression(File tmp, File gz) {
        ExecutorService exec = maintenanceExecutor;
        if (exec == null || state != State.RUNNING) {
            tmp.renameTo(new File(tmp.getPath().replace(".compressing", "")));
            return;
        }

        exec.execute(() -> compressLogFile(tmp, gz));
    }

    private void compressLogFile(@NotNull File tmp, @NotNull File gz) {
        try (GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(gz.toPath()))) {
            // Copy the entire contents of the temporary file into the GZIP stream
            Files.copy(tmp.toPath(), gzip);

            // Delete the temporary .compressing file
            deleteFile(tmp);

            // Check if compressed file exceeds the size limit and delete if so
            if (gz.length() >
                    LoggerManager.getSettings().getCompressedFileSizeLimit() * 1024L) {
                deleteFile(gz);
            }
        } catch (IOException e) {
            LoggerManager.internalLog("Compression failed for " + tmp, e);
            tmp.renameTo(new File(tmp.getPath().replace(".compressing", "")));
        }
    }

    private @NotNull String generateSessionId() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS"));
    }

    private void enforceFileLimit() {
        int limit = LoggerManager.getSettings().getLogFileLimit();
        if (limit < 0) return;

        // Get all .log and .gz files, ignoring current log file
        List<File> files = getSortedLogFiles();

        // Delete oldest files until we're under the limit
        while (files.size() > limit) {
            File oldest = files.remove(0);
            deleteFile(oldest);
        }
    }

    /**
     * @return A list sorted in ascending order by "last modified", containing all log files (compressed and uncompressed) inside the current log directory except the current log file.
     */
    private @NotNull List<File> getSortedLogFiles() {
        File dir = Path.of(LoggerManager.getSettings().getLogDirectory()).toFile();

        File[] files = dir.listFiles(f -> {
            String name = f.getName();
            try {
                return (name.endsWith(".log") || name.endsWith(".gz")) &&
                        (!f.getCanonicalPath().equals(currentLogFileCanonicalPath));
            } catch (IOException e) {
                LoggerManager.internalLog("Failed to check canonical path for file: " + f, e);
                return false;
            }
        });

        if (files == null) return List.of();

        List<File> list = new ArrayList<>(Arrays.asList(files));
        list.sort(Comparator.comparingLong(File::lastModified));
        return list;
    }

    private void deleteFile(@NotNull File f) {
        if (!f.delete()) {
            LoggerManager.internalLog("Failed to delete file: " + f);
        }
    }
}
