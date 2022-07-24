import adapter.BrightnessSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Controller<ID> {
    private static final int DETECTION_INTERVAL = 5;
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private boolean isStopped = false;
    private final BrightnessSensor<ID> brightnessSensor;

    private final Executor executor = CompletableFuture.delayedExecutor(DETECTION_INTERVAL, TimeUnit.SECONDS);

    public Controller(final BrightnessSensor<ID> brightnessSensor) {
        this.brightnessSensor = brightnessSensor;
    }

    public CompletableFuture<Void> execute() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        schedule2(() -> brightnessSensor.detect().thenAccept(d -> {
                LOG.info("Detected brightness: {}", d);
                if(isStopped) {
                    future.complete(null);
                }
            }
        ));
//        return schedule(() -> brightnessSensor.detect().thenAccept(d -> {
//                LOG.info("Detected brightness: {}", d);
//                execute();
//            })true
//        );
        return future;
    }

    private CompletableFuture<Void> schedule(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    private void schedule2(Runnable runnable) {
        final ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor();
        executor2.scheduleAtFixedRate(runnable,0, DETECTION_INTERVAL, TimeUnit.SECONDS);
    }

    public void stop() {
        this.isStopped = true;
    }
}
