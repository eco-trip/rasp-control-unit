import adapter.BrightnessSensor;
import adapter.Pi4jProvider;
import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.Bus;
import io.github.ecotrip.sensors.DetectionFactory;

import java.util.UUID;

public class Application {
    public static void main(String[] args) throws Exception {
        final Console console = new Console();

        // Print program title/header
        console.title("<-- The Pi4J Project -->", "Minimal Example project");

        var pi4j = Pi4J.newAutoContext();

        BrightnessSensor<UUID> sensor = new BrightnessSensor.Builder<UUID>(pi4j)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(Address.of(0x23))
                .setBus(Bus.of(1))
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(DetectionFactory.of(UUID::randomUUID))
                .build();

        new Controller<>(sensor).execute();

        pi4j.shutdown();
    }
}
