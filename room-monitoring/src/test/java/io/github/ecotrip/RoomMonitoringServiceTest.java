package io.github.ecotrip;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.execution.engine.EngineFactory;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.measure.water.FlowRate;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.usecase.ConsumptionUseCases;
import io.github.ecotrip.usecase.EnvironmentUseCases;

@ExtendWith(MockitoExtension.class)
public class RoomMonitoringServiceTest {
    @Test
    public void testStart() {
        var engine = EngineFactory.createScheduledEngine(1);
        var detectionFactory = DetectionFactory.of(UUID::randomUUID);

        // Mock behaviours
        var current = Current.of(5);
        var mockedCurrentSensor = MockedSensor.of(UUID.randomUUID(), detectionFactory)
                .setMockedBehaviour(List.of(current));
        var hotFlowRate = FlowRate.of(10, FlowRate.FlowRateType.HOT);
        var mockedHotFlowRateSensor = MockedSensor.of(UUID.randomUUID(), detectionFactory)
                .setMockedBehaviour(List.of(hotFlowRate));
        var temperature = Temperature.of(20, Temperature.Environment.HOT_WATER_PIPE);
        var mockedTemperatureSensor = MockedSensor.of(UUID.randomUUID(), detectionFactory)
                .setMockedBehaviour(List.of(temperature));

        //Create use cases
        var consumptionUseCases = new ConsumptionUseCases.Builder<UUID>()
                .setHotFlowRateSensor(mockedHotFlowRateSensor)
                .setCurrentSensor(mockedCurrentSensor)
                .build();
        var environmentUseCases = new EnvironmentUseCases.Builder<UUID>()
                .setHotWaterTemperatureSensor(mockedTemperatureSensor)
                .build();

        var payload = new AtomicReference<>("");
        var interval = Execution.SECOND_IN_MILLIS * 2;

        var service = RoomMonitoringService.of(engine, consumptionUseCases, environmentUseCases,
                detectionFactory, msg -> {
                    payload.set(msg);
                    return CompletableFuture.completedFuture(null);
                }, element -> element.getDetection().toString());
        service.setDetectionInterval(interval);

        var fut = service.start();
        Execution.safeSleep(interval);

        // var expCurrent = Current.of(current.getValue());
        // var expHotFlowRate = Current.of(hotFlowRate.getValue());

        // assertTrue(payload.get().contains(expCurrent.toString()));
        // assertTrue(payload.get().contains(expHotFlowRate.toString()));
        // assertTrue(payload.get().contains(temperature.toString()));
        fut.complete(null);
        fut.join();
    }
}
