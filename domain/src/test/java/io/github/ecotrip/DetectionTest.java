package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.sensor.Detection;

public class DetectionTest {
    @Test
    public void testDetectionByEquals() {
        var d1 = Detection.empty("abc123");
        var d2 = Detection.empty("abc456");
        var measures = List.of(Current.of(15), Temperature.of(20, Temperature.Environment.ROOM));
        var d3 = Detection.of("abc123", measures);
        assertNotEquals(d1, d2);
        assertEquals(d1, d3);
        assertEquals(d3.getMeasures(), measures);
        assertEquals(d1.getMeasures(), List.of());
        assertEquals(d1.toString(), "Detection{"
                + "identifier=abc123, "
                + "detectionTime=" + d1.getDetectionTime()
                + ", measures=" + d1.getMeasures()
                + "}");
    }

    @Test
    public void testDetectionInstant() {
        var d1 = Detection.empty("abc123");
        var d2 = Detection.empty("abc456");
        assertTrue(d1.getDetectionTime().isBefore(d2.getDetectionTime()));
    }
}
