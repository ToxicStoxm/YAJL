package com.toxicstoxm.YAJL;

import com.sun.jdi.NativeMethodException;
import org.junit.jupiter.api.Test;

public class LoggerTest {

    private static final Logger LOGGER = Logger.autoConfigureLogger();

    @Test
    public void APITest() {
        LOGGER.log(LogLevels.ERROR, "Hello this is an error! Message: {}", 45);

        LOGGER.stacktrace("Hello");
        LOGGER.stacktrace(new IndexOutOfBoundsException("Shit"));
        LOGGER.stacktrace("Hello {}", 47);
        LOGGER.verbose("Hey");
        LOGGER.verbose("Hey {}", 'O');
        LOGGER.debug("KP");
        LOGGER.debug(new char[]{'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!'});
        LOGGER.debug("Hey {}", 56.456345643);
        LOGGER.info("hello");
        LOGGER.info("KVM qemu {}", "2€€€€€");
        LOGGER.warn("Maybe");
        LOGGER.warn("shittify {}", "hannes");
        LOGGER.error("Something went wrong!");
        LOGGER.error(new IllegalAccessException("You can't do that!"));
        LOGGER.error("Lul u stopid! {} {}", "+", 25);
        LOGGER.error("NOOOOO", new NullPointerException("You are nullllll!"));
        LOGGER.error("You went wrong! {} {} {}", new NativeMethodException("Oh oh SIGSEGV!"), "dsfasf", 5.3, 'A');
        LOGGER.fatal(new RuntimeException("OHH no a fatal exception", new IndexOutOfBoundsException("Sheet metal")));
        LOGGER.fatal("oh oh {}", 5.456);
        LOGGER.fatal("Hello ", new IndexOutOfBoundsException("Off by 1 error, you stopid!"));
        LOGGER.fatal("I wanted to tell you that.{}..", new IllegalThreadStateException("This is not allowed state!"), 7.8);

        LOGGER.debug((LogMessagePlaceholder) () -> "Hello World");
    }

}
