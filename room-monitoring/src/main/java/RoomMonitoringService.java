import engine.Engine;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Futures;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RoomMonitoringService<ID, M extends Measure<?>, S extends Sensor<ID, M>> {
    private static final int DETECTION_INTERVAL = 5;
    private static final Logger LOG = LoggerFactory.getLogger(RoomMonitoringService.class);

    private final List<S> sensors;

    private final Engine service;

    private RoomMonitoringService(final List<S> sensors, final Engine service) {
        this.sensors = sensors;
        this.service = service;
    }

    public void start() {
        service.schedule(() -> {
            var futures = sensors.parallelStream().map(Sensor::detect).collect(Collectors.toUnmodifiableList());
            Futures.thenAll(futures, this::logsDetections);
        }, DETECTION_INTERVAL);
        service.waitScheduledJobs();
    }
    
    private CompletableFuture<Void> logsDetections(final List<Detection<ID, M>> detections) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        service.schedule(() -> {
            detections.forEach(d -> LOG.info("-> {}", d));
            future.complete(null);
        });
        return future;
    }

    public static <ID, M extends Measure<?>, S extends Sensor<ID, M>> RoomMonitoringService<ID, M, S> of(
            final List<S> sensors, final Engine engine) {
        return new RoomMonitoringService<>(sensors, engine);
    }
}
