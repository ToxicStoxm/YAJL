package com.toxicstoxm.YAJL.colors;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class ColorConverterTest {

    @Test
    void getColorFromHex() {
        Color color = new Color(34, 241, 221);
        assertEquals(color, ColorConverter.getColorFromHex("#22F1DD"));
    }

    @Test
    void mixColors() {
        Color color = new Color(34, 241, 221, 0);
        assertEquals(color, ColorConverter.mixColors(
                new Color(31, 232, 255, 0),
                new Color(38, 250, 188, 0)
        ));
        assertEquals(color, ColorConverter.mixColors(
                new Color(31, 232, 255, 0),
                new Color(38, 252, 192, 0),
                new Color(36, 232, 255, 0),
                new Color(38, 251, 190, 0)
        ));
    }
}