import com.toxicstoxm.YAJL.LoggerTests;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import org.junit.jupiter.api.Test;
import utils.LogCapture;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraceTests {
    private static final Logger logger = LoggerManager.getLogger(LoggerTests.class);
    private static final Logger virtuallogger = LoggerManager.getVirtualLogger("SomeVirutalArea");

    @Test
    void traceDefaultFormatTest() {
        try (LogCapture cap = new LogCapture()) {
            LoggerManager.configure()
                    .addOutput(cap.ps)
                    .logMessageLayout("{trace}: {message}")
                    .done();

            logger.info("TraceTest");

            String out = cap.read();

            assertTrue(out.contains("TraceTests"));
            assertTrue(out.contains("traceDefaultFormatTest"));
            assertTrue(out.matches(".*:\\d+: TraceTest.*\\n"));
        }
    }

    @Test
    void traceSelectiveFieldsTest() {
        try (LogCapture cap = new LogCapture()) {
            LoggerManager.configure()
                    .addOutput(cap.ps)
                    .logMessageLayout("{trace:class,method,separator=->}")
                    .done();

            logger.info("ignored");
            assertTrue(cap.read().startsWith("TraceTests->traceSelectiveFieldsTest"));
        }
    }
}
