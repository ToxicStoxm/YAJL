package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.core.LogFilter;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerTests {
    private static final Logger logger = LoggerManager.getLogger(LoggerTests.class);
    private static final Logger virtuallogger = LoggerManager.getVirtualLogger("SomeVirutalArea");

    @BeforeEach
    public void setup() {
        LoggerManager.resetSettings();
    }

    @Test
    public void logFilterTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure(new File("/home/dominik/Downloads/test.yml"))
                .addOutput(ps)
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

    @Test
    public void layoutWithoutColorsTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .enableColorCoding(false)
                .logMessageLayout("[{level}] {message}")
                .done();

        logger.info("No Color");

        assertEquals("[INFO] No Color\n", bs.toString());
    }

    @Test
    public void prefixResolutionTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .logMessageLayout("{prefix}: {message}")
                .done();

        logger.info("ClassLogger");
        virtuallogger.info("VirtualLogger");

        String[] lines = bs.toString().split("\n");

        assertEquals("LoggerTests: ClassLogger", lines[0]);
        assertEquals("SomeVirutalArea: VirtualLogger", lines[1]);
    }

    @Test
    public void traceDefaultFormatTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .logMessageLayout("{trace}: {message}")
                .done();

        logger.info("TraceTest");

        String out = bs.toString();

        assertTrue(out.contains("LoggerTests"));
        assertTrue(out.contains("traceDefaultFormatTest"));
        assertTrue(out.matches(".*:\\d+: TraceTest.*\\n"));
    }

    @Test
    public void traceSelectiveFieldsTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .logMessageLayout("{trace:class,method,separator=->}")
                .done();

        logger.info("ignored");

        String out = bs.toString();

        assertTrue(out.startsWith("LoggerTests->traceSelectiveFieldsTest"));
    }

    @Test
    public void colorPlaceholderDisabledTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .logMessageLayout("{color:hex=#FF0000}RED {message}")
                .enableColorCoding(false)
                .done();

        logger.info("TEXT");

        assertEquals("RED TEXT\033[0m\n", bs.toString());
    }

    @Test
    public void layoutCacheInvalidationTest() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);

        LoggerManager.configure()
                .addOutput(ps)
                .logMessageLayout("A {message}")
                .done();

        logger.info("X");
        assertEquals("A X\n", bs.toString());
        bs.reset();

        LoggerManager.configure()
                .logMessageLayout("B {message}")
                .done();

        logger.info("Y");
        assertEquals("B Y\n", bs.toString());
    }

}
