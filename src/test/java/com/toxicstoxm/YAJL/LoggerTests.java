package com.toxicstoxm.YAJL;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class LoggerTests {
    private static final Logger logger = LoggerManager.getLogger(LoggerTests.class);
    private static final Logger virtuallogger = LoggerManager.getVirtualLogger("SomeVirutalArea");

    @Test
    public void generalTest() {
        LoggerManager.getInstance(new File("/home/dominik/Downloads/test.yaml"));

        logger.log("Hello World!");
        virtuallogger.log("Hello World!");

        boolean isFiltered = LoggerManager.getSettings().getLogFilter().isFiltered("Hello World!");
        System.out.println(isFiltered);

        isFiltered = LoggerManager.getSettings().getLogFilter().isFiltered("Hello World!");
        System.out.println(isFiltered);

        LoggerManager.configure()
                .addLogFilterPattern("Hello")
                .done();

        isFiltered = LoggerManager.getSettings().getLogFilter().isFiltered("Hello World!");
        System.out.println(isFiltered);
    }
}
