package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Brightness;
import io.github.ecotrip.measure.ambient.Humidity;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.sensor.DetectionFactory;

@ExtendWith(MockitoExtension.class)
public class EnvironmentUseCasesTest {
    private final DetectionFactory<UUID> detectionFactory = DetectionFactory.of(UUID::randomUUID);
    private final MockedSensor mockedSensor = Mockito.mock(MockedSensor.class, Mockito.withSettings()
            .useConstructor(UUID.randomUUID(), detectionFactory)
            .defaultAnswer(Mockito.CALLS_REAL_METHODS)
    );

    private final List<Measure> temperatureAndHumidityMeasure = List.of(Temperature.of(25,
            Temperature.Environment.ROOM), Humidity.of(47));
    private final List<Measure> hotWaterPipeMeasure = List.of(Temperature.of(70,
            Temperature.Environment.HOT_WATER_PIPE));
    private final List<Measure> coldWaterPipeMeasure = List.of(Temperature.of(20,
            Temperature.Environment.COLD_WATER_PIPE));
    private final List<Measure> brightnessMeasure = List.of(Brightness.of(100));

    @Test
    public void testUseCasesWithoutSensors() {
        var emptyUseCases = new EnvironmentUseCases.Builder<UUID>().build();
        var f1 = emptyUseCases.detectColdWaterTemperature();
        var f2 = emptyUseCases.detectHotWaterTemperature();
        var f3 = emptyUseCases.detectRoomBrightness();
        var f4 = emptyUseCases.detectRoomTemperatureAndHumidity();

        assertTrue(f1.isCompletedExceptionally());
        assertTrue(f2.isCompletedExceptionally());
        assertTrue(f3.isCompletedExceptionally());
        assertTrue(f4.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyColdWaterTemperatureSensor() {
        mockedSensor.setMockedBehaviour(coldWaterPipeMeasure);
        var useCases = new EnvironmentUseCases.Builder<UUID>()
                .setColdWaterTemperatureSensor(mockedSensor)
                .build();

        var f1 = useCases.detectColdWaterTemperature();
        var f2 = useCases.detectHotWaterTemperature();
        var f3 = useCases.detectRoomBrightness();
        var f4 = useCases.detectRoomTemperatureAndHumidity();

        assertEquals(f1.join().getMeasures(), coldWaterPipeMeasure);
        assertTrue(f2.isCompletedExceptionally());
        assertTrue(f3.isCompletedExceptionally());
        assertTrue(f4.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyHotWaterTemperatureSensor() {
        mockedSensor.setMockedBehaviour(hotWaterPipeMeasure);
        var useCases = new EnvironmentUseCases.Builder<UUID>()
                .setHotWaterTemperatureSensor(mockedSensor)
                .build();

        var f1 = useCases.detectColdWaterTemperature();
        var f2 = useCases.detectHotWaterTemperature();
        var f3 = useCases.detectRoomBrightness();
        var f4 = useCases.detectRoomTemperatureAndHumidity();

        assertEquals(f2.join().getMeasures(), hotWaterPipeMeasure);
        assertTrue(f1.isCompletedExceptionally());
        assertTrue(f3.isCompletedExceptionally());
        assertTrue(f4.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyRoomTemperatureAndHumiditySensor() {
        mockedSensor.setMockedBehaviour(temperatureAndHumidityMeasure);
        var useCases = new EnvironmentUseCases.Builder<UUID>()
                .setTemperatureAndHumiditySensor(mockedSensor)
                .build();

        var f1 = useCases.detectColdWaterTemperature();
        var f2 = useCases.detectHotWaterTemperature();
        var f3 = useCases.detectRoomBrightness();
        var f4 = useCases.detectRoomTemperatureAndHumidity();

        assertEquals(f4.join().getMeasures(), temperatureAndHumidityMeasure);
        assertTrue(f2.isCompletedExceptionally());
        assertTrue(f1.isCompletedExceptionally());
        assertTrue(f3.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyRoomBrightnessSensor() {
        mockedSensor.setMockedBehaviour(brightnessMeasure);
        var useCases = new EnvironmentUseCases.Builder<UUID>()
                .setBrightnessSensor(mockedSensor)
                .build();

        var f1 = useCases.detectColdWaterTemperature();
        var f2 = useCases.detectHotWaterTemperature();
        var f3 = useCases.detectRoomBrightness();
        var f4 = useCases.detectRoomTemperatureAndHumidity();

        assertEquals(f3.join().getMeasures(), brightnessMeasure);
        assertTrue(f2.isCompletedExceptionally());
        assertTrue(f1.isCompletedExceptionally());
        assertTrue(f4.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithAllSensors() {
        var useCases = new EnvironmentUseCases.Builder<UUID>()
                .setColdWaterTemperatureSensor(mockedSensor)
                .setBrightnessSensor(mockedSensor)
                .setHotWaterTemperatureSensor(mockedSensor)
                .setTemperatureAndHumiditySensor(mockedSensor)
                .build();

        mockedSensor.setMockedBehaviour(coldWaterPipeMeasure);
        var f1 = useCases.detectColdWaterTemperature();
        assertEquals(f1.join().getMeasures(), coldWaterPipeMeasure);

        mockedSensor.setMockedBehaviour(hotWaterPipeMeasure);
        var f2 = useCases.detectHotWaterTemperature();
        assertEquals(f2.join().getMeasures(), hotWaterPipeMeasure);

        mockedSensor.setMockedBehaviour(brightnessMeasure);
        var f3 = useCases.detectRoomBrightness();
        assertEquals(f3.join().getMeasures(), brightnessMeasure);

        mockedSensor.setMockedBehaviour(temperatureAndHumidityMeasure);
        var f4 = useCases.detectRoomTemperatureAndHumidity();
        assertEquals(f4.join().getMeasures(), temperatureAndHumidityMeasure);
    }
}
