package com.toxicstoxm.YAJL;

import org.junit.jupiter.api.Test;

import java.io.File;

public class LoggerTests {
    private final Logger logger = new Logger(getClass());

    @Test
    public void generalTest() {
        LoggerManager.getInstance(new File("/home/dominik/Downloads/test.yaml"));

        System.out.println(LoggerManager.getSettings().getTest());

        logger.log("Hello World!");
    }
}
