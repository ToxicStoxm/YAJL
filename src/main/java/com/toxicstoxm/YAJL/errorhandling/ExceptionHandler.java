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

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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


    public static void handle(@NotNull Throwable throwable, @Nullable String userMessage, @Nullable LogLevel logLevel, @Nullable CustomErrorHandler customErrorHandler) {
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

        // Parse the name of the class
        String originClassPath = originElement.getClassName();

        // Get the logger and the animation file
        Logger targetLogger = originClass == null ? null : ClassTools.getLoggerFromClass(originClass);
        String originClassSimpleName = originClass == null ? "Unknown" : originClass.getSimpleName();

        // Spoof the logger if either the class can't be obtained or doesn't have a logger
        if (targetLogger == null) {
            LoggerManager.internalLog("Failed to get logger of \"" + originClassPath + "\"! Spoofing logger instead...");
            targetLogger = LoggerManager.getVirtualLogger(originClassPath, originClassSimpleName);
        }

        // Construct and print the message
        String extraInfo = constructMessage(throwable, userMessage, originClassSimpleName, trace[trace.length - 1].getClassName());
        if (customErrorHandler != null) {
            customErrorHandler.handle(targetLogger, originClass, extraInfo);
        }
        targetLogger.log(logLevel, extraInfo);
    }

    public static void handle(@NotNull Throwable throwable) {
        handle(throwable, null, null, null);
    }

    public static void handle(@NotNull Throwable throwable, @Nullable String userMessage) {
        handle(throwable, userMessage, null, null);
    }

    public static void handle(@NotNull Throwable throwable, @Nullable LogLevel logLevel) {
        handle(throwable, null, logLevel, null);
    }
}
