package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.sensor.Detection;
import io.github.ecotrip.sensor.DetectionFactory;

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

    @Test
    public void testFactory() {
        var factory = DetectionFactory.of(UUID::randomUUID);
        var m1 = List.of(Current.of(15), Temperature.of(20, Temperature.Environment.ROOM));
        List<Measure> m2 = List.of(Temperature.of(10, Temperature.Environment.COLD_WATER_PIPE));
        var d1 = factory.create(m1);
        var d2 = factory.create(m2);
        var mergedDetection = factory.merge(d1, d2);
        assertTrue(mergedDetection.getMeasures().containsAll(m1));
        assertTrue(mergedDetection.getMeasures().containsAll(m2));
        assertTrue(mergedDetection.getDetectionTime().isAfter(d2.getDetectionTime()));
        var d3 = factory.createEmpty();
        assertEquals(d3.getMeasures(), List.of());
    }
}
