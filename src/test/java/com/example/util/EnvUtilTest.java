package com.example.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvUtilTest {

    @Test
    void parsePositiveInt_returnsDefaultOnNullOrBlank() {
        assertEquals(5, EnvUtil.parsePositiveInt(null, 5, 1, 10));
        assertEquals(5, EnvUtil.parsePositiveInt(" ", 5, 1, 10));
    }

    @Test
    void parsePositiveInt_clampsToMinAndMax() {
        assertEquals(1, EnvUtil.parsePositiveInt("-10", 5, 1, 10));
        assertEquals(10, EnvUtil.parsePositiveInt("999", 5, 1, 10));
    }

    @Test
    void parsePositiveInt_returnsParsedWhenInRange() {
        assertEquals(7, EnvUtil.parsePositiveInt("7", 5, 1, 10));
        assertEquals(1, EnvUtil.parsePositiveInt("1", 5, 1, 10));
        assertEquals(10, EnvUtil.parsePositiveInt("10", 5, 1, 10));
    }

    @Test
    void parsePositiveInt_trimsAndHandlesInvalid() {
        assertEquals(8, EnvUtil.parsePositiveInt(" 8 ", 5, 1, 10));
        assertEquals(5, EnvUtil.parsePositiveInt("not-a-number", 5, 1, 10));
    }

    @Test
    void defaultIfBlank_behavesAsExpected() {
        assertEquals("def", EnvUtil.defaultIfBlank(null, "def"));
        assertEquals("def", EnvUtil.defaultIfBlank("   ", "def"));
        assertEquals("abc", EnvUtil.defaultIfBlank("abc", "def"));
        assertEquals("abc", EnvUtil.defaultIfBlank("  abc  ", "def"));
    }
}
