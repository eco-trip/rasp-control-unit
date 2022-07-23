import adapter.BrightnessSensor;
import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.sensors.Detection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Controller<ID> {
    private static int DETECTION_INTERVAL = 1000;
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private boolean isStopped = false;
    private final BrightnessSensor<ID> brightnessSensor;

    public Controller(final BrightnessSensor<ID> brightnessSensor) {
        this.brightnessSensor = brightnessSensor;
    }

    public void execute() {
        CompletableFuture.runAsync(() -> {
            while(!isStopped) {
                try {
                    final Detection<ID, Brightness> detection = brightnessSensor.detect()
                            .get(DETECTION_INTERVAL, TimeUnit.MILLISECONDS);
                    LOG.info("Detected brightness: {}", detection);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        this.isStopped = true;
    }
}
