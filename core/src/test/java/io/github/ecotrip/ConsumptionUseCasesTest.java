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
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.measure.water.FlowRate;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.usecase.ConsumptionUseCases;

@ExtendWith(MockitoExtension.class)
public class ConsumptionUseCasesTest {
    private final DetectionFactory<UUID> detectionFactory = DetectionFactory.of(UUID::randomUUID);
    private final MockedSensor mockedSensor = Mockito.mock(MockedSensor.class, Mockito.withSettings()
            .useConstructor(UUID.randomUUID(), detectionFactory)
            .defaultAnswer(Mockito.CALLS_REAL_METHODS)
    );

    @Test
    public void testUseCasesWithoutSensors() {
        var emptyUseCases = new ConsumptionUseCases.Builder<UUID>().build();
        var f1 = emptyUseCases.detectColdFlowRate();
        var f2 = emptyUseCases.detectCurrent();
        var f3 = emptyUseCases.detectHotFlowRate();
        assertTrue(f1.isCompletedExceptionally());
        assertTrue(f2.isCompletedExceptionally());
        assertTrue(f3.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyColdFlowRateSensor() {
        List<Measure> validMeasures = List.of(FlowRate.of(5, FlowRate.FlowRateType.COLD));
        mockedSensor.setMockedBehaviour(validMeasures);

        var useCases = new ConsumptionUseCases.Builder<UUID>()
                .setColdFlowRateSensor(mockedSensor)
                .build();
        var f1 = useCases.detectColdFlowRate();
        var f2 = useCases.detectCurrent();
        var f3 = useCases.detectHotFlowRate();

        assertEquals(f1.join().getMeasures(), validMeasures);
        assertTrue(f2.isCompletedExceptionally());
        assertTrue(f3.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyHotFlowRateSensor() {
        List<Measure> validMeasures = List.of(FlowRate.of(5, FlowRate.FlowRateType.HOT));
        mockedSensor.setMockedBehaviour(validMeasures);

        var useCases = new ConsumptionUseCases.Builder<UUID>()
                .setHotFlowRateSensor(mockedSensor)
                .build();
        var f1 = useCases.detectColdFlowRate();
        var f2 = useCases.detectHotFlowRate();
        var f3 = useCases.detectCurrent();

        assertTrue(f1.isCompletedExceptionally());
        assertEquals(f2.join().getMeasures(), validMeasures);
        assertTrue(f3.isCompletedExceptionally());
    }

    @Test
    public void testUseCasesWithOnlyCurrentSensor() {
        List<Measure> validMeasures = List.of(Current.of(5));
        mockedSensor.setMockedBehaviour(validMeasures);

        var useCases = new ConsumptionUseCases.Builder<UUID>()
                .setCurrentSensor(mockedSensor)
                .build();
        var f1 = useCases.detectColdFlowRate();
        var f2 = useCases.detectHotFlowRate();
        var f3 = useCases.detectCurrent();

        assertTrue(f1.isCompletedExceptionally());
        assertTrue(f2.isCompletedExceptionally());
        assertEquals(f3.join().getMeasures(), validMeasures);
    }

    @Test
    public void testUseCasesWithAllSensors() {
        var useCases = new ConsumptionUseCases.Builder<UUID>()
                .setColdFlowRateSensor(mockedSensor)
                .setHotFlowRateSensor(mockedSensor)
                .setCurrentSensor(mockedSensor)
                .build();

        List<Measure> coldFlowRateMeasure = List.of(FlowRate.of(5, FlowRate.FlowRateType.COLD));
        mockedSensor.setMockedBehaviour(coldFlowRateMeasure);
        var d1 = useCases.detectColdFlowRate().join();
        assertEquals(d1.getMeasures(), coldFlowRateMeasure);

        List<Measure> hotFlowRateMeasure = List.of(FlowRate.of(50, FlowRate.FlowRateType.HOT));
        mockedSensor.setMockedBehaviour(hotFlowRateMeasure);

        var d2 = useCases.detectHotFlowRate().join();
        assertEquals(d2.getMeasures(), hotFlowRateMeasure);

        List<Measure> currentMeasure = List.of(Current.of(12));
        mockedSensor.setMockedBehaviour(currentMeasure);

        var d3 = useCases.detectCurrent().join();
        assertEquals(d3.getMeasures(), currentMeasure);
    }
}
