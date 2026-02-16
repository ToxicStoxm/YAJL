package com.toxicstoxm.YAJL.errorhandling;

import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerConfig;
import com.toxicstoxm.YAJL.core.LoggerManager;
import com.toxicstoxm.YAJL.core.level.LogLevel;
import com.toxicstoxm.YAJL.util.tools.ClassTools;
import com.toxicstoxm.YAJL.util.tools.StringTools;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionHandler {
    private ExceptionHandler() {}

    private static @NotNull String craftRecursiveStackTrace(@NotNull Throwable throwable) {
        Deque<Throwable> chainDeque = ClassTools.getExceptionChain(throwable);
        List<Throwable> chain = new ArrayList<>(chainDeque); // preserve order

        int maxFrames = LoggerManager.getSettings().getStackTraceLengthLimit();

        StringBuilder sb = new StringBuilder(1024);
        String lastMessageSignature = null;

        // Iterate from innermost to outermost
        for (int e = chain.size() - 1; e >= 0; e--) {

            Throwable current = chain.get(e);
            StackTraceElement[] currentTrace = current.getStackTrace();

            sb.append(current.getClass().getSimpleName()).append('\n');

            String message = current.getMessage();
            String signature = current.getClass().getName() + ": " + message;

            if (message != null && !message.isBlank() && !signature.equals(lastMessageSignature)) {
                sb.append("  |- Message: \"").append(message).append("\"\n");
            }

            lastMessageSignature = signature;

            sb.append("  \\- Trace\n");

            // Compare against next OUTER exception
            StackTraceElement[] outerTrace =
                    (e > 0) ? chain.get(e - 1).getStackTrace() : null;

            int commonFrames = 0;

            if (outerTrace != null) {
                int i = currentTrace.length - 1;
                int j = outerTrace.length - 1;

                while (i >= 0 && j >= 0 && currentTrace[i].equals(outerTrace[j])) {
                    commonFrames++;
                    i--;
                    j--;
                }
            }

            int printUntil = currentTrace.length - commonFrames;

            // Enforce max frames
            if (maxFrames > 0 && printUntil > maxFrames) {
                printUntil = maxFrames;
            }

            for (int i = 0; i < printUntil; i++) {
                if (i < printUntil - 1) {
                    sb.append("       |--> ");
                } else {
                    sb.append("       \\--> ");
                }
                sb.append(currentTrace[i]).append('\n');
            }

            int truncatedFrames = (currentTrace.length - commonFrames) - printUntil;
            if (truncatedFrames > 0) {
                sb.append("       \\--> ... ").append(truncatedFrames).append(" more\n");
            }
        }

        return sb.toString();
    }

    private static @NotNull String constructMessage(
            @NotNull Throwable throwable,
            @NotNull String userMessage,
            @NotNull String originClassName,
            @NotNull String callerClassName) {

        LoggerConfig settings = LoggerManager.getSettings();
        int padding = settings.getExceptionHandlerPaddingSize();
        int spacing = settings.getExceptionHandlerEmptySpaces();

        StringBuilder body = new StringBuilder(2048);

        body.append("Handled in ").append(callerClassName).append(".\n");

        if (!userMessage.isBlank()) {
            if (userMessage.indexOf('\n') >= 0) {
                body.append('\n');
            }
            body.append(userMessage).append('\n');
        }

        body.append('\n');
        body.append(craftRecursiveStackTrace(throwable));

        String bodyString = body.toString();
        int maxLineLength = StringTools.getLongestLineLength(bodyString);

        String headerMessage = throwable.getClass().getSimpleName()
                + " thrown in " + originClassName;

        int headerLength = headerMessage.length() + (padding + spacing) * 2;

        if ((maxLineLength - headerLength) % 2 != 0) {
            maxLineLength++;
        }

        int sideLength = ((maxLineLength - headerMessage.length()) / 2) - spacing;

        String halfSeparator = "-".repeat(Math.max(0, sideLength));
        String whitespace = " ".repeat(spacing);

        return halfSeparator +
                whitespace +
                headerMessage +
                whitespace +
                halfSeparator +
                '\n' +
                bodyString +
                "-".repeat(maxLineLength);
    }


    public static void handle(@NotNull Throwable throwable, @Nullable String userMessage, @Nullable LogLevel logLevel) {
        // Defaults here to allow function overloads to be simpler
        if (userMessage == null) {
            userMessage = "";
        }

        if (logLevel == null) {
            logLevel = LoggerManager.getSettings().getExceptionHandlerDefaultLogLevel();
        }

        // Get the class that threw the exception as well as the class handling it
        StackTraceElement[] trace = throwable.getStackTrace();
        StackTraceElement originElement = trace[0];
        Class<?> originClass = ClassTools.getClassFromTraceElement(originElement);
        /*if (originClass != null && originClass.isAnnotationPresent(ErrorHandling.class)) {
            ErrorHandling e = originClass.getAnnotation(ErrorHandling.class);
            String customMessage = e.customMessage();
            if (!customMessage.isBlank()) {
                Pattern pattern = Pattern.compile("\\{([^}]+)}");
                Matcher matcher = pattern.matcher(customMessage);

                while (matcher.find()) {
                    String placeholder = matcher.group(1); // group 1 captures the content inside {}
                    for (Field f : originClass.getDeclaredFields()) {
                        if (f.isAnnotationPresent(ErrorVariable.class)) {
                            ErrorVariable er = f.getAnnotation(ErrorVariable.class);
                            if (er.name().equals(placeholder)) {
                                f.setAccessible(true);

                                // replace
                            }
                        }
                    }
                }
            }
        }*/

        // Parse the name of the class
        ClassName originClassName = ClassName.parse(originElement.getClassName());

        // Get the logger and the animation file
        Logger targetLogger = originClass == null ? null : ClassTools.getLoggerFromClass(originClass);
        // Spoof the logger if either the class can't be obtained or doesn't have a logger
        if (targetLogger == null) {
            LoggerManager.internalLog("Failed to get logger of \"" + originClassName.path + "\"! Spoofing logger instead...");
            targetLogger = LoggerManager.getVirtualLogger(originClassName.path, originClassName.simpleName);
        }

        // Construct and print the message
        String source = originClassName.simpleName;
        String extraInfo = constructMessage(throwable, userMessage, source, trace[trace.length - 1].getClassName());
        targetLogger.log(logLevel, extraInfo);
    }

    public static void handle(@NotNull Throwable throwable) {
        handle(throwable, null, null);
    }

    public static void handle(@NotNull Throwable throwable, @Nullable String userMessage) {
        handle(throwable, userMessage, null);
    }

    public static void handle(@NotNull Throwable throwable, @Nullable LogLevel logLevel) {
        handle(throwable, null, logLevel);
    }

    public record ClassName(String path, String simpleName, String simplePackage) {
        @Contract("_ -> new")
        public static @NotNull ClassName parse(@NotNull String path) {
            int lastDot = path.lastIndexOf('.');
            if (lastDot < 0) {
                throw new IllegalArgumentException("Invalid ClassPath provided: \"" + path + "\"");
            }

            int secondLastDot = path.lastIndexOf('.', lastDot - 1);

            String simpleName = path.substring(lastDot + 1);
            String simplePackage = secondLastDot < 0
                    ? path.substring(0, lastDot)
                    : path.substring(secondLastDot + 1, lastDot);

            return new ClassName(path, simpleName, simplePackage);
        }
    }
}
