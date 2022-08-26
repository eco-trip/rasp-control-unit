package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.measure.ambient.Brightness;

public class MeasureTest {
    @Test
    public void testBrightnessByEquals() {
        var b1 = Brightness.of(300);
        var b2 = Brightness.of(300);
        var b3 = Brightness.of(100);
        assertEquals(b1, b2);
        assertEquals(b1, b1);
        assertNotEquals(b1, b3);
    }

    @Test
    public void testBrightnessOps() {
        var b1 = Brightness.of(300);
        var b2 = Brightness.of(300);
        var b3 = Brightness.of(100);
        assertTrue(b1.isGreaterEqualThan(b2));
        assertTrue(b2.isGreaterEqualThan(b3));
        assertTrue(b3.isLessEqualThan(b2));
        assertFalse(b3.isGreaterEqualThan(b1));
        assertEquals(b1.toString(), "Brightness{value=" + b1.getValue() + " lux}");
    }
}
