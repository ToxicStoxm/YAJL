package com.toxicstoxm.YAJL.util.tools;

import org.jetbrains.annotations.NotNull;

public class StringTools {
    public static int getLongestLineLength(@NotNull String string) {
        int max = 0;
        int current = 0;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == '\n') {
                if (current > max) {
                    max = current;
                }
                current = 0;
            } else if (c != '\r') {
                current++;
            }
        }

        return Math.max(max, current);
    }
}
