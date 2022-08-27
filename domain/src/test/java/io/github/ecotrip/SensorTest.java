package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

public class SensorTest {
    private static final double MAX_ROOM_TEMP = 40;
    private static final double MIN_ROOM_TEMP = 10;
    private static final DetectionFactory<UUID> detectionFactory = DetectionFactory.of(UUID::randomUUID);
    private final Function<Double, Boolean> measureValidator = v -> v >= MIN_ROOM_TEMP && v <= MAX_ROOM_TEMP;

    @BeforeAll
    static void testDetectionFactory() {
        final Measure m1 = Temperature.of(10, Temperature.Environment.HOT_WATER_PIPE);
        final Measure m2 = Current.of(20);

        var emptyDetection = detectionFactory.createEmpty();
        var validDetection = detectionFactory.create(List.of(m1, m2));
        assertEquals(validDetection.getMeasures().get(0), m1);
        assertEquals(validDetection.getMeasures().get(1), m2);
        assertNotEquals(emptyDetection.getIdentifier(), validDetection.getIdentifier());
        assertTrue(emptyDetection.getDetectionTime().isBefore(validDetection.getDetectionTime()));
        var mergedDetection = detectionFactory.merge(emptyDetection, validDetection);
        assertEquals(mergedDetection.getMeasures(), validDetection.getMeasures());
        var mergedDetection2 = detectionFactory.merge(validDetection, validDetection);
        assertEquals(mergedDetection2.getMeasures().size(), validDetection.getMeasures().size() * 2);
    }

    @Test
    public void testFaultySensor() {
        var identifier = UUID.randomUUID();
        final Sensor<UUID> faultySensor = new Sensor<>(identifier, detectionFactory) {
            @Override
            protected CompletableFuture<List<Measure>> measure() {
                var tooCold = Temperature.of(MIN_ROOM_TEMP - 1, Temperature.Environment.ROOM);
                var tooHot = Temperature.of(MAX_ROOM_TEMP + 1, Temperature.Environment.ROOM);
                return CompletableFuture.completedFuture(List.of(tooCold, tooHot));
            }

            @Override
            protected boolean isMeasureValid(Measure measure) {
                return measureValidator.apply(measure.getValue());
            }
        };

        var detection = faultySensor.detect().join();
        assertTrue(detection.getMeasures().isEmpty());
        assertEquals(faultySensor.getIdentifier(), identifier);
        var detection2 = faultySensor.detect().join();
        assertTrue(detection.getDetectionTime().isBefore(detection2.getDetectionTime()));
    }

    @Test
    public void testSampleSensor() {
        final Sensor<UUID> sampleSensor = new Sensor<>(UUID.randomUUID(), detectionFactory) {
            private final Supplier<Double> valuesGenerator = () -> Math.floor(Math.random()
                    * (MAX_ROOM_TEMP - MIN_ROOM_TEMP + 1) + MIN_ROOM_TEMP);

            @Override
            protected CompletableFuture<List<Measure>> measure() {
                var roomTemperature = Temperature.of(valuesGenerator.get(), Temperature.Environment.ROOM);
                return CompletableFuture.completedFuture(List.of(roomTemperature));
            }

            @Override
            protected boolean isMeasureValid(Measure measure) {
                return measureValidator.apply(measure.getValue());
            }
        };

        IntStream.of(0, 5).parallel().forEach(i -> {
            var detection = sampleSensor.detect().join();
            assertFalse(detection.getMeasures().isEmpty());
            detection.getMeasures().forEach(d -> assertTrue(measureValidator.apply(d.getValue())));
        });
    }
}
