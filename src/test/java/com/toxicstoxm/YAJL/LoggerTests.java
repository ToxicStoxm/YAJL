package com.toxicstoxm.YAJL;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerTests {
    private static final Logger logger = LoggerManager.getLogger(LoggerTests.class);
    private static final Logger virtuallogger = LoggerManager.getVirtualLogger("SomeVirutalArea");

    @Test
    public void generalTest() throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);


        LoggerManager.configure()
                .output(ps)
                .done();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("*"))
                .filterPatternsAsBlacklist(false)
                .done();

        logger.info("Match !Blacklist");
        assertFalse(bs.toString().isEmpty());
        bs.writeTo(System.out);
        bs.reset();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("abcde"))
                .filterPatternsAsBlacklist(false)
                .done();

        logger.info("!Match Blacklist");
        assertTrue(bs.toString().isEmpty());
        bs.writeTo(System.out);
        bs.reset();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("*"))
                .filterPatternsAsBlacklist(true)
                .done();

        logger.info("Match Blacklist");
        assertTrue(bs.toString().isEmpty());
        bs.writeTo(System.out);
        bs.reset();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("abcde"))
                 .filterPatternsAsBlacklist(true)
                 .done();

        logger.info("!Match Blacklist");
        assertFalse(bs.toString().isEmpty());
        bs.writeTo(System.out);
        bs.reset();

        LoggerManager.resetSettings();

        long start = System.nanoTime();
        LoggerManager.getSettings().getLogFilter().isFiltered("Hello World!");
        long end = System.nanoTime();
        long diff1 = end - start;
        System.out.println("No cache: " + diff1);

        start = System.nanoTime();
        LoggerManager.getSettings().getLogFilter().isFiltered("Hello World!");
        end = System.nanoTime();
        long diff2 = end - start;
        System.out.println("Cache: " + diff2);

        LoggerManager.configure()
                .addLogFilterPattern("Hello")
                .done();

        start = System.nanoTime();
        LoggerManager.getSettings().getLogFilter().isFiltered("Hello World!");
        end = System.nanoTime();
        long diff3 = end - start;
        System.out.println("Cleared cache: " + diff3);

        assertTrue(diff2 < diff1 && diff2 < diff3);

        LoggerManager.resetSettings();
    }
}
