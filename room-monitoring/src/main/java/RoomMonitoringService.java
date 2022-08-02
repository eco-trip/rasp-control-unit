import engine.Engine;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.DetectionFactory;
import object.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import usecases.ConsumptionUseCases;
import usecases.EnvironmentUseCases;
import execution.Futures;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class RoomMonitoringService<ID> {
    private static final int DETECTION_INTERVAL = 3;
    private static final Logger LOG = LoggerFactory.getLogger(RoomMonitoringService.class);
    private final ConsumptionUseCases<ID> consumptionUseCases;
    private final EnvironmentUseCases<ID> environmentUseCases;
    private final DetectionFactory<ID> detectionFactory;
    private final Engine<Detection<ID>> service;

    private RoomMonitoringService(final Engine<Detection<ID>> service,
                                  final ConsumptionUseCases<ID> consumptionUseCases,
                                  final EnvironmentUseCases<ID> environmentUseCases,
                                  final DetectionFactory<ID> detectionFactory) {
        this.service = service;
        this.consumptionUseCases = consumptionUseCases;
        this.environmentUseCases = environmentUseCases;
        this.detectionFactory = detectionFactory;
    }

    public void start() {
        service.schedule(() -> {
            var currentConsumption = computeConsumptions(consumptionUseCases::detectCurrent).thenApply(d ->
                    computeAverage(d, p -> Current.of((p.getValue1().getValue() + p.getValue2().getValue())/2)));
            var flowRateConsumption = computeConsumptions(consumptionUseCases::detectHotFlowRate).thenApply(d ->
                    computeAverage(d, p -> FlowRate.of(p.getValue1().getValue() + p.getValue2().getValue())));

            var futures = List.of(
                    environmentUseCases.detectRoomBrightness(),
                    environmentUseCases.detectRoomTemperatureAndHumidity(),
                    environmentUseCases.detectHotWaterTemperature(),
                    currentConsumption, flowRateConsumption);

            Futures.thenAll(futures, this::logsDetections);
        }, DETECTION_INTERVAL);
        service.waitExecution();
    }

    private CompletableFuture<Detection<ID>> computeConsumptions(
            final Supplier<CompletableFuture<Detection<ID>>> detectionSupplier) {
        final var initialAccumulator = CompletableFuture.completedFuture(detectionFactory.createEmpty());
        return service.submitAndRepeat(acc -> detectionSupplier.get()
                .thenCombine(acc, detectionFactory::merge), initialAccumulator, 5, 1);
    }
    
    private CompletableFuture<Void> logsDetections(final List<Detection<ID>> detections) {
        final var future = new CompletableFuture<Void>();
        service.submit(() -> {
            detections.forEach(d -> LOG.info("{}", d));
            future.complete(null);
        });
        return future;
    }

    private Detection<ID> computeAverage(final Detection<ID> detection,
                                         final Function<Pair<Measure, Measure>, Measure> combineFun) {
        var measure = detection.getMeasures().stream()
                .reduce((m1, m2) -> combineFun.apply(Pair.of(m1,m2)));
        return measure.isPresent() ? detectionFactory.create(List.of(measure.get())) : detectionFactory.createEmpty();
    }

    public static <ID> RoomMonitoringService<ID> of(final Engine<Detection<ID>> engine,
                                                    final ConsumptionUseCases<ID> consumptionUseCases,
                                                    final EnvironmentUseCases<ID> environmentUseCases,
                                                    final DetectionFactory<ID> detectionFactory) {
        return new RoomMonitoringService<>(engine, consumptionUseCases, environmentUseCases, detectionFactory);
    }
}
