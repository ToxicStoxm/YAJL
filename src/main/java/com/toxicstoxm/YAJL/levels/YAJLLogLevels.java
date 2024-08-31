package com.toxicstoxm.YAJL.levels;

import java.awt.*;

public class YAJLLogLevels {

    public static class Fatal extends YAJLLogLevel {
        public Fatal(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Error extends YAJLLogLevel {
        public Error(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Warn extends YAJLLogLevel {
        public Warn(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Info extends YAJLLogLevel {
        public Info(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Debug extends YAJLLogLevel {
        public Debug(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Verbose extends YAJLLogLevel {
        public Verbose(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Stacktrace extends YAJLLogLevel {
        public Stacktrace(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }
}
