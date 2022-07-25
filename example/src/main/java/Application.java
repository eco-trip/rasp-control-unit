import adapter.BrightnessSensor;
import adapter.Pi4jProvider;
import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import engine.EngineFactory;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.I2cBus;
import io.github.ecotrip.sensors.DetectionFactory;

import java.util.List;
import java.util.UUID;

public class Application {
    public static void main(String[] args) {
        final Console console = new Console();

        console.title("<-- The Pi4J Project -->", "Rasp Control Unit System");

        var pi4j = Pi4J.newAutoContext();

        var sensor = new BrightnessSensor.Builder<UUID>(pi4j)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(Address.of(0x23))
                .setBus(I2cBus.one())
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(DetectionFactory.of(UUID::randomUUID))
                .build();

        RoomMonitoringService.of(List.of(sensor), EngineFactory.createScheduledExecutor()).start();

        pi4j.shutdown();
    }
}
