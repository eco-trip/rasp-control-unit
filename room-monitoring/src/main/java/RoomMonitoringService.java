import engine.Engine;
import io.github.ecotrip.sensors.Detection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import usecases.ConsumptionUseCases;
import usecases.EnvironmentUseCases;
import utils.Futures;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RoomMonitoringService<ID> {
    private static final int DETECTION_INTERVAL = 5;
    private static final Logger LOG = LoggerFactory.getLogger(RoomMonitoringService.class);
    private final ConsumptionUseCases<ID> consumptionUseCases;
    private final EnvironmentUseCases<ID> environmentUseCases;

    private final Engine service;

    private RoomMonitoringService(final Engine service, final ConsumptionUseCases<ID> consumptionUseCases,
                                  final EnvironmentUseCases<ID> environmentUseCases) {
        this.service = service;
        this.consumptionUseCases = consumptionUseCases;
        this.environmentUseCases = environmentUseCases;
    }

    public void start() {
        service.schedule(() -> {
            var futures = List.of(
                    environmentUseCases.detectRoomBrightness(),
                    environmentUseCases.detectRoomTemperatureAndHumidity(),
                    consumptionUseCases.detectCurrent(),
                    consumptionUseCases.detectFlowRate());
            Futures.thenAll(futures, this::logsDetections);
        }, DETECTION_INTERVAL);
        service.waitExecution();
    }
    
    private CompletableFuture<Void> logsDetections(final List<Detection<ID>> detections) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        service.submit(() -> {
            detections.forEach(d -> LOG.info("{}", d));
            future.complete(null);
        });
        return future;
    }

    public static <ID> RoomMonitoringService<ID> of(final Engine engine,
                                                    final ConsumptionUseCases<ID> consumptionUseCases,
                                                    final EnvironmentUseCases<ID> environmentUseCases) {
        return new RoomMonitoringService<>(engine, consumptionUseCases, environmentUseCases);
    }
}
