package com.toxicstoxm.YAJL.tools;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ColorTools {

    /**
     * Converts a java.awt.Color object to an ANSI escape code for foreground text color.
     *
     * @param color The Color object to convert.
     * @return The ANSI escape code as a string.
     */
    public static @NotNull String toAnsi(Color color) {
        if (color == null) {
            color = Color.decode("#FFFFFF");
        }

        // Get RGB components
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // Return ANSI escape code in RGB format
        return String.format("\033[38;2;%d;%d;%dm", red, green, blue);
    }


    /**
     * Resets ANSI styles to default.
     *
     * @return The ANSI escape code to reset colors.
     */
    @Contract(pure = true)
    public static @NotNull String resetAnsi() {
        return "\033[0m";
    }

    /**
     * Strips ANSI color codes from a given string.
     *
     * @param input The string potentially containing ANSI color codes.
     * @return The string with ANSI color codes removed.
     */
    public static @NotNull String stripAnsi(@NotNull String input) {
        return input.replaceAll("\033\\[[;\\d]*m", "");
    }
}
