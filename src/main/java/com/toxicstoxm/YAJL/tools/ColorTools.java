package com.toxicstoxm.YAJL.tools;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Random;

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
     * Converts an ANSI escape code for a foreground text color back to a java.awt.Color object.
     *
     * @param ansi The ANSI escape code string.
     * @return The corresponding Color object, or null if the format is invalid.
     */
    public static @Nullable Color fromAnsi(@NotNull String ansi) {
        if (!ansi.matches("\033\\[38;2;\\d{1,3};\\d{1,3};\\d{1,3}m")) {
            return null; // Invalid format
        }

        try {
            // Extract RGB values
            String[] parts = ansi.substring(7, ansi.length() - 1).split(";");
            int red = Integer.parseInt(parts[0]);
            int green = Integer.parseInt(parts[1]);
            int blue = Integer.parseInt(parts[2]);

            return new Color(red, green, blue);
        } catch (Exception e) {
            return null; // Return null on parsing errors
        }
    }


    /**
     * Generates a random color based on the string input seed.
     * @param seed seed used for generating a random color
     * @return the random color, color will be equal the same seed
     */
    @Contract(pure = true)
    public static @NotNull Color randomColor(@NotNull String seed) {
        long hash = seed.hashCode();

        Random rand = new Random(hash);
        return new Color(Math.abs(rand.nextInt()) % 256, Math.abs(rand.nextInt()) % 256, Math.abs(rand.nextInt()) % 256);
    }

    @Contract("_ -> new")
    public static @NotNull Color mixColors(Color @NotNull ... colors) {
        float ratio = 1f / ((float) colors.length);
        int r = 0, g = 0, b = 0, a = 0;
        for (Color color : colors) {
            r += (int) (color.getRed() * ratio);
            g += (int) (color.getGreen() * ratio);
            b += (int) (color.getBlue() * ratio);
            a += (int) (color.getAlpha() * ratio);
        }
        return new Color(r, g, b, a);
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
