import adapter.BrightnessSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Controller<ID> {
    private static final int DETECTION_INTERVAL = 5;
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private boolean isStopped = false;
    private final BrightnessSensor<ID> brightnessSensor;

    public Controller(final BrightnessSensor<ID> brightnessSensor) {
        this.brightnessSensor = brightnessSensor;
    }

    public CompletableFuture<Void> execute() {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(DETECTION_INTERVAL, TimeUnit.SECONDS);
        return CompletableFuture.runAsync(() -> {
            var detection = brightnessSensor.detect();
            detection.thenAccept(d -> LOG.info("Detected brightness: {}", d));
        }, delayedExecutor);
    }

    public void stop() {
        this.isStopped = true;
    }
}
