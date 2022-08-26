package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.measure.ambient.Brightness;
import io.github.ecotrip.measure.ambient.Humidity;

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

    @Test
    public void testHumidityByEquals() {
        var h1 = Humidity.of(55);
        var h2 = Humidity.of(55);
        var h3 = Humidity.of(10);
        assertEquals(h1, h2);
        assertEquals(h1, h1);
        assertNotEquals(h1, h3);
    }

    @Test
    public void testHumidityOps() {
        var h1 = Humidity.of(55);
        var h2 = Humidity.of(55);
        var h3 = Humidity.of(10);
        assertTrue(h1.isGreaterEqualThan(h2));
        assertTrue(h2.isGreaterEqualThan(h3));
        assertTrue(h3.isLessEqualThan(h1));
        assertFalse(h3.isGreaterEqualThan(h1));
        assertEquals(h1.toString(), "Humidity{value=" + h1.getValue() + "%}");
    }
}
