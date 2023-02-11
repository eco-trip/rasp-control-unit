package io.github.ecotrip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.ecotrip.adapter.DetectionWrapper;
import io.github.ecotrip.adapter.OutputAdapter;
import io.github.ecotrip.adapter.Serializer;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.execution.Futures;
import io.github.ecotrip.execution.engine.Engine;
import io.github.ecotrip.measure.CombinableMeasure;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.MeasureType;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.measure.water.FlowRate;
import io.github.ecotrip.pattern.Observer;
import io.github.ecotrip.sensor.Detection;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.token.Token;
import io.github.ecotrip.usecase.ConsumptionUseCases;
import io.github.ecotrip.usecase.EnvironmentUseCases;

/**
 * Contains the logic necessary to collect the data from the sensors, serialize
 * the final {@link Detection} and send it through an {@link OutputAdapter}.
 */
public class RoomMonitoringService implements Observer<Token> {
    /**
     * Default sampling interval
     */
    public static final int DEFAULT_DETECT_INTERVAL_SEC = 5;
    private static final int DEFAULT_DETECT_INTERVAL_MILLIS = Execution.SECOND_IN_MILLIS * DEFAULT_DETECT_INTERVAL_SEC;
    private static final Map<MeasureType, Function<Double, Measure>> measureMap;
    static {
        measureMap = new HashMap<>();
        measureMap.put(MeasureType.CURRENT, Current::of);
        measureMap.put(MeasureType.COLD_FLOW_RATE, v -> FlowRate.of(v, FlowRate.FlowRateType.COLD));
        measureMap.put(MeasureType.HOT_FLOW_RATE, v -> FlowRate.of(v, FlowRate.FlowRateType.HOT));
    }
    private final ConsumptionUseCases<UUID> consumptionUseCases;
    private final EnvironmentUseCases<UUID> environmentUseCases;
    private final DetectionFactory<UUID> detectionFactory;
    private final Engine engine;
    private final OutputAdapter<String, String> outputAdapter;
    private final Serializer<DetectionWrapper> serializer;
    private int detectionInterval;
    private int consumptionRepetitions;
    private String stayId;

    private RoomMonitoringService(final Engine engine,
                                  final ConsumptionUseCases<UUID> consumptionUseCases,
                                  final EnvironmentUseCases<UUID> environmentUseCases,
                                  final DetectionFactory<UUID> detectionFactory,
                                  final OutputAdapter<String, String> outputAdapter,
                                  final Serializer<DetectionWrapper> serializer) {
        this.engine = engine;
        this.consumptionUseCases = consumptionUseCases;
        this.environmentUseCases = environmentUseCases;
        this.detectionFactory = detectionFactory;
        this.outputAdapter = outputAdapter;
        this.serializer = serializer;
        setDetectionInterval(DEFAULT_DETECT_INTERVAL_MILLIS);
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
            Futures.thenAll(futures, this::sendData, DEFAULT_DETECT_INTERVAL_MILLIS);
        }, detectionInterval);
    }


    public void setDetectionInterval(int intervalInMillis) {
        detectionInterval = intervalInMillis;
        consumptionRepetitions = detectionInterval / Execution.SECOND_IN_MILLIS;
    }

    private CompletableFuture<Void> sendData(final List<Detection<UUID>> detections) {
        var mergedDetection = detections.stream().reduce(detectionFactory::merge);
        if (mergedDetection.isPresent()) {
            var message = this.serializer.serialize(
                    DetectionWrapper.of(mergedDetection.get(), DEFAULT_DETECT_INTERVAL_SEC, stayId));
            return outputAdapter.sendMessage(message)
                    .thenRun(() -> Execution.logsInfo("Send message: " + message));
        }
        return CompletableFuture.failedFuture(new Throwable("An error is occurred during the detection"));
    }

    private CompletableFuture<Detection<UUID>> computeConsumptionAverage(
            final Supplier<CompletableFuture<Detection<UUID>>> detectionSupplier) {
        final var initialAccumulator = CompletableFuture.completedFuture(detectionFactory.createEmpty());
        return engine.submitAndRepeat(acc -> detectionSupplier.get()
                        .thenCombine(acc, detectionFactory::merge), initialAccumulator, consumptionRepetitions, 1)
                .thenApply(this::computeAverage);
    }

    private Detection<UUID> computeAverage(final Detection<UUID> detection) {
        var optMeasure = detection.getMeasures().stream()
                .map(m -> (CombinableMeasure) m)
                .reduce(CombinableMeasure::checkAndCombine);

        if (optMeasure.isEmpty()) {
            return detectionFactory.createEmpty();
        }

        var measureType = optMeasure.get().getType();
        double measureValue = optMeasure.get().getValue() / ((double)detectionInterval / Execution.SECOND_IN_MILLIS);

        return detectionFactory.create(List.of(measureMap.get(measureType).apply(measureValue)));
    }

    /**
     * Factory method to create a {@link RoomMonitoringService} instance.
     * @param engine used to run the service's tasks inside a specific context.
     * @param consumptionUseCases used to retrieve the room consumption.
     * @param environmentUseCases used to detect the room environment factors.
     * @param detectionFactory simplify the {@link Detection} creation.
     * @param outputAdapter used to send the data outside.
     * @param serializer adapts the data format to the one required outside.
     * @return the instance of {@link RoomMonitoringService}.
     * */
    public static RoomMonitoringService of(final Engine engine,
                                           final ConsumptionUseCases<UUID> consumptionUseCases,
                                           final EnvironmentUseCases<UUID> environmentUseCases,
                                           final DetectionFactory<UUID> detectionFactory,
                                           final OutputAdapter<String, String> outputAdapter,
                                           final Serializer<DetectionWrapper> serializer) {
        return new RoomMonitoringService(engine, consumptionUseCases, environmentUseCases, detectionFactory,
                outputAdapter, serializer);
    }

    @Override
    public void notify(final Token token) {
        this.stayId = JwtParser.getPayload(token.getValue()).getString("stayId");
    }
}
