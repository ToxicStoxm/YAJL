package com.toxicstoxm.YAJL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggerTests {
    private static final Logger logger = LoggerManager.getLogger(LoggerTests.class);
    private static final Logger virtuallogger = LoggerManager.getVirtualLogger("SomeVirutalArea");

    @BeforeEach
    public void setup() {
        LoggerManager.resetSettings();
    }

    @Test
    public void logFilterTest() {
        LoggerManager.getInstance(new File("/home/dominik/Downloads/test.yml"));

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .done();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("*"))
                .filterPatternsAsBlacklist(false)
                .done();

        logger.info("Match !Blacklist");
        assertFalse(bs.toString().isEmpty());
        bs.reset();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("abcde"))
                .filterPatternsAsBlacklist(false)
                .done();

        logger.info("!Match Blacklist");
        assertTrue(bs.toString().isEmpty());
        bs.reset();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("*"))
                .filterPatternsAsBlacklist(true)
                .done();

        logger.info("Match Blacklist");
        assertTrue(bs.toString().isEmpty());
        bs.reset();

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("abcde"))
                 .filterPatternsAsBlacklist(true)
                 .done();

        logger.info("!Match Blacklist");
        assertFalse(bs.toString().isEmpty());
        bs.reset();
    }

    @Test
    public void cachingTest() throws NoSuchFieldException, IllegalAccessException {
        LogFilter lf = LoggerManager.getSettings().getLogFilter();
        Field f = lf.getClass().getDeclaredField("cache");
        f.setAccessible(true);
        HashMap<String, Boolean> val = (HashMap<String, Boolean>) f.get(lf);
        Supplier<Boolean> check = () -> val.containsKey("Hello World!");

        assertFalse(check.get());
        lf.isFiltered("Hello World!");
        assertTrue(check.get() && val.get("Hello World!") && !lf.isFiltered("Hello World!"));

        LoggerManager.configure()
                .addLogFilterPattern("Hello")
                .done();

        assertFalse(check.get());
        lf.isFiltered("Hello World!");
        assertTrue(check.get() && val.get("Hello World!") && !lf.isFiltered("Hello World!"));

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("alsdfj"))
                .done();

        assertFalse(check.get());
        lf.isFiltered("Hello World!");
        assertTrue(check.get() && !val.get("Hello World!") && lf.isFiltered("Hello World!"));

        LoggerManager.configure()
                .addLogFilterPattern("alsdfj")
                .done();

        assertTrue(check.get());
        lf.isFiltered("Hello World!");
        assertTrue(check.get() && !val.get("Hello World!") && lf.isFiltered("Hello World!"));

        LoggerManager.configure()
                .logAreaFilterPatterns(List.of("alsdfj"))
                .done();

        assertTrue(check.get());
        lf.isFiltered("Hello World!");
        assertTrue(check.get() && !val.get("Hello World!") && lf.isFiltered("Hello World!"));

    }
}
