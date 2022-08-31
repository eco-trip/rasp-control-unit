package io.github.ecotrip;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.ecotrip.adapter.OutputAdapter;
import io.github.ecotrip.adapter.Serializer;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.execution.Futures;
import io.github.ecotrip.execution.engine.Engine;
import io.github.ecotrip.measure.CombinableMeasure;
import io.github.ecotrip.sensor.Detection;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.usecase.ConsumptionUseCases;
import io.github.ecotrip.usecase.EnvironmentUseCases;

/**
 * Contains the logic necessary to collect the data from the sensors, serialize
 * the final {@link Detection} and send it through an {@link OutputAdapter}.
 * @param <ID>
 */
public class RoomMonitoringService<ID> {
    private static final String DEFAULT_TOPIC = "ecotrip/detection";
    private static final int DEFAULT_DETECT_INTERVAL = Execution.SECOND_IN_MILLIS * 5;
    private final ConsumptionUseCases<ID> consumptionUseCases;
    private final EnvironmentUseCases<ID> environmentUseCases;
    private final DetectionFactory<ID> detectionFactory;
    private final Engine engine;
    private final OutputAdapter<String, String> outputAdapter;
    private final Serializer<Detection<ID>> serializer;
    private int detectionInterval;
    private int consumptionRepetitions;

    private RoomMonitoringService(final Engine engine,
                                  final ConsumptionUseCases<ID> consumptionUseCases,
                                  final EnvironmentUseCases<ID> environmentUseCases,
                                  final DetectionFactory<ID> detectionFactory,
                                  final OutputAdapter<String, String> outputAdapter,
                                  final Serializer<Detection<ID>> serializer) {
        this.engine = engine;
        this.consumptionUseCases = consumptionUseCases;
        this.environmentUseCases = environmentUseCases;
        this.detectionFactory = detectionFactory;
        this.outputAdapter = outputAdapter;
        this.serializer = serializer;
        setDetectionInterval(DEFAULT_DETECT_INTERVAL);
    }

    /**
     * Launches the service using the provided {@link Engine}.
     * @return a {@link CompletableFuture} which represents the process on running state until finish.
     */
    public CompletableFuture<Void> start() {
        return engine.schedule(() -> {
            var currentConsumption = computeConsumptionAverage(consumptionUseCases::detectCurrent);
            var hotFlowRateConsumption = computeConsumptionAverage(consumptionUseCases::detectHotFlowRate);
            var coldFlowRateConsumption = computeConsumptionAverage(consumptionUseCases::detectColdFlowRate);
            var futures = List.of(
                    environmentUseCases.detectRoomBrightness(),
                    environmentUseCases.detectRoomTemperatureAndHumidity(),
                    environmentUseCases.detectHotWaterTemperature(),
                    environmentUseCases.detectColdWaterTemperature(),
                    currentConsumption, coldFlowRateConsumption, hotFlowRateConsumption
            );
            Futures.thenAll(futures, this::sendData, DEFAULT_DETECT_INTERVAL);
        }, detectionInterval);
    }


    public void setDetectionInterval(int intervalInMillis) {
        detectionInterval = intervalInMillis;
        consumptionRepetitions = detectionInterval / Execution.SECOND_IN_MILLIS;
    }

    private CompletableFuture<Void> sendData(final List<Detection<ID>> detections) {
        var mergedDetection = detections.stream().reduce(detectionFactory::merge);
        if (mergedDetection.isPresent()) {
            var message = this.serializer.serialize(mergedDetection.get());
            return outputAdapter.sendMessage(DEFAULT_TOPIC, message)
                    .thenRun(() -> Execution.logsInfo("Send message: " + message));
        }
        return CompletableFuture.failedFuture(new Throwable("An error is occurred during the detection"));
    }

    private CompletableFuture<Detection<ID>> computeConsumptionAverage(
            final Supplier<CompletableFuture<Detection<ID>>> detectionSupplier) {
        final var initialAccumulator = CompletableFuture.completedFuture(detectionFactory.createEmpty());
        return engine.submitAndRepeat(acc -> detectionSupplier.get()
                        .thenCombine(acc, detectionFactory::merge), initialAccumulator, consumptionRepetitions, 1)
                .thenApply(this::computeAverage);
    }

    private Detection<ID> computeAverage(final Detection<ID> detection) {
        var measure = detection.getMeasures().stream()
                .map(m -> (CombinableMeasure) m)
                .reduce(CombinableMeasure::checkAndCombine);
        return measure.isPresent() ? detectionFactory.create(List.of(measure.get())) : detectionFactory.createEmpty();
    }

    /**
     * Factory method to create a {@link RoomMonitoringService} instance.
     * @param engine used to run the service's tasks inside a specific context.
     * @param consumptionUseCases used to retrieve the room consumption.
     * @param environmentUseCases used to detect the room environment factors.
     * @param detectionFactory simplify the {@link Detection} creation.
     * @param outputAdapter used to send the data outside.
     * @param serializer adapts the data format to the one required outside.
     * @param <ID> represents the identifier's type.
     * @return the instance of {@link RoomMonitoringService}.
     * */
    public static <ID> RoomMonitoringService<ID> of(final Engine engine,
                                                    final ConsumptionUseCases<ID> consumptionUseCases,
                                                    final EnvironmentUseCases<ID> environmentUseCases,
                                                    final DetectionFactory<ID> detectionFactory,
                                                    final OutputAdapter<String, String> outputAdapter,
                                                    final Serializer<Detection<ID>> serializer) {
        return new RoomMonitoringService<>(engine, consumptionUseCases, environmentUseCases, detectionFactory,
                outputAdapter, serializer);
    }
}
