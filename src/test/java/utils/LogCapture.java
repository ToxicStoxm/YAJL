package utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LogCapture implements AutoCloseable {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    public final PrintStream ps = new PrintStream(out);

    public String read() {
        return out.toString();
    }

    public void reset() {
        out.reset();
    }

    @Override
    public void close() {
        ps.close();
    }
}
