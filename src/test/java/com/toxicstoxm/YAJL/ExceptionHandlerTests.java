package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import com.toxicstoxm.YAJL.errorhandling.ClassLogger;
import com.toxicstoxm.YAJL.errorhandling.ExceptionHandler;
import org.junit.jupiter.api.Test;

public class ExceptionHandlerTests {
    @ClassLogger
    private static final MyOwnKripplFichLogger sdfsdfs = new MyOwnKripplFichLogger("hello", "fichl");

    public static class MyOwnKripplFichLogger extends Logger {

        protected MyOwnKripplFichLogger(String area, String prefix) {
            super(area, prefix);
        }
    }

    @Test
    void test() {
        LoggerManager.configure()
                .minimumLogLevel(-5000)
                .done();

        long start = System.currentTimeMillis();
        try {
            try {
                testing();
            } catch (RuntimeException e) {
                throw e;
            }
        } catch (Throwable t) {
            ExceptionHandler.handle(t);
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");

        start = System.currentTimeMillis();
        ExceptionHandler.handle(new NullPointerException("Hello World!"));
        end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
    }

    void testing() {
        try {
            try {
                throw new NullPointerException("Hello World!");
            } catch (NullPointerException e) {
                throw new RuntimeException("Shit", e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("jdhsgjsdfg", e);
        }
    }
}