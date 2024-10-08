package com.udacity.catpoint.security;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StyleServiceTest {

    @Test
    void testHeadingFontProperties() {
        // Assert that the font family is "Sans Serif"
        assertEquals("Sans Serif", StyleService.HEADING_FONT.getName(), "Font name should be Sans Serif");

        // Assert that the font style is bold
        assertEquals(Font.BOLD, StyleService.HEADING_FONT.getStyle(), "Font style should be bold");

        // Assert that the font size is 24
        assertEquals(24, StyleService.HEADING_FONT.getSize(), "Font size should be 24");
    }
}
