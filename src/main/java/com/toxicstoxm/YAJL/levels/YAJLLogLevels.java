package com.toxicstoxm.YAJL.levels;

import com.toxicstoxm.YAJL.YAJLSettingsBundle;
import com.toxicstoxm.YAJL.colors.ColorConverter;

public class YAJLLogLevels {

    public static class Fatal extends YAJLLogLevel {
        public Fatal() {
            super(
                    YAJLSettingsBundle.EnableFatalLevel.getInstance().get(),
                    YAJLSettingsBundle.FatalText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.FatalColor.getInstance().get())
            );
        }
    }

    public static class Error extends YAJLLogLevel {
        public Error() {
            super(
                    YAJLSettingsBundle.EnableErrorLevel.getInstance().get(),
                    YAJLSettingsBundle.ErrorText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.ErrorColor.getInstance().get())
            );
        }
    }

    public static class Warn extends YAJLLogLevel {
        public Warn() {
            super(
                    YAJLSettingsBundle.EnableWarnLevel.getInstance().get(),
                    YAJLSettingsBundle.WarnText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.WarnColor.getInstance().get())
            );
        }
    }

    public static class Info extends YAJLLogLevel {
        public Info() {
            super(
                    YAJLSettingsBundle.EnableInfoLevel.getInstance().get(),
                    YAJLSettingsBundle.InfoText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.InfoColor.getInstance().get())
            );
        }
    }

    public static class Debug extends YAJLLogLevel {
        public Debug() {
            super(
                    YAJLSettingsBundle.EnableDebugLevel.getInstance().get(),
                    YAJLSettingsBundle.DebugText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.DebugColor.getInstance().get())
            );
        }
    }

    public static class Verbose extends YAJLLogLevel {
        public Verbose() {
            super(
                    YAJLSettingsBundle.EnableVerboseLevel.getInstance().get(),
                    YAJLSettingsBundle.VerboseText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.VerboseColor.getInstance().get())
            );
        }
    }

    public static class Stacktrace extends YAJLLogLevel {
        public Stacktrace() {
            super(
                    YAJLSettingsBundle.EnableStacktraceLevel.getInstance().get(),
                    YAJLSettingsBundle.StacktraceText.getInstance().get(),
                    ColorConverter.getColorFromHex(YAJLSettingsBundle.StacktraceColor.getInstance().get())
            );
        }
    }
}
