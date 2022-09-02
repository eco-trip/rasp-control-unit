package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.exception.Pi4JException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.plugin.mock.provider.i2c.MockI2CProvider;

import io.github.ecotrip.adapter.sensor.BH1750Sensor;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Brightness;
import io.github.ecotrip.sensor.Detection;
import io.github.ecotrip.sensor.DetectionFactory;

public class SensorsTest {
    private static final DetectionFactory<UUID> detectionFactory = DetectionFactory.of(UUID::randomUUID);
    private Context pi4j;

    @BeforeEach
    public void beforeTest() throws Pi4JException {
        // Initialize Pi4J with auto context
        // An auto context enabled AUTO-DETECT loading
        // which will load any detected Pi4J extension
        // libraries (Platforms and Providers) from the class path
        pi4j = Pi4J.newAutoContext();
        assertNotNull(pi4j.providers());
    }

    @AfterEach
    public void afterTest() {
        try {
            pi4j.shutdown();
        } catch (Pi4JException e) { /* do nothing */ }
    }

    @Test
    public void testBH1750() throws IOException {
        // create random set of sample data
        var sampleA = "0A".getBytes(StandardCharsets.UTF_8); // equals to 12353.0 lux
        var sampleB = "0B".getBytes(StandardCharsets.UTF_8); // equals to 12354.0 lux
        var sampleC = "0C".getBytes(StandardCharsets.UTF_8); // equals to 12355.0 lux

        // create I2C config
        var config = I2C.newConfigBuilder(pi4j)
                .id("my-i2c-bus")
                .name("My I2C Bus")
                .bus(1)
                .device(0x04)
                .build();

        // use try-with-resources to auto-close I2C when complete
        try (var i2c = MockI2CProvider.newInstance().create(config)) {

            var brightnessSensor = new BH1750Sensor.Builder<UUID>()
                    .setI2C(i2c)
                    .setIdentifier(UUID.randomUUID())
                    .setDetectionFactory(detectionFactory)
                    .build();

            waitAndExtractMeasure(brightnessSensor.detect()); // skip first measure

            i2c.out().write(sampleA);
            assertEquals(Brightness.of(12353), waitAndExtractMeasure(brightnessSensor.detect()));
            i2c.out().write(sampleB);
            assertEquals(Brightness.of(12354), waitAndExtractMeasure(brightnessSensor.detect()));
            i2c.out().write(sampleC);
            assertEquals(Brightness.of(12355), waitAndExtractMeasure(brightnessSensor.detect()));

            assertEquals(Brightness.of(0), waitAndExtractMeasure(brightnessSensor.detect()));
        }
    }

    private static Measure waitAndExtractMeasure(CompletableFuture<Detection<UUID>> future) {
        return future.join().getMeasures().get(0);
    }
}
