import adapter.ADSConverter;
import adapter.BrightnessSensor;
import adapter.Pi4jProvider;
import adapter.TemperatureSensor;
import adapter.builder.I2cBuilder;
import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import engine.EngineFactory;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.measures.energy.Resistance;
import io.github.ecotrip.measures.energy.Voltage;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.adc.AnalogChannel;
import io.github.ecotrip.sensors.adc.Gain;
import io.github.ecotrip.sensors.channel.I2cBus;
import io.github.ecotrip.sensors.DetectionFactory;

import java.util.List;
import java.util.UUID;

public class Application {
    public static void main(String[] args) {
        final Console console = new Console();

        console.title("<-- The Pi4J Project -->", "Rasp Control Unit System");

        var pi4j = Pi4J.newAutoContext();

        var channel1 = new I2cBuilder<UUID>(pi4j)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(Address.of(0x23))
                .setBus(I2cBus.ONE)
                .setIdentifier(UUID.randomUUID())
                .build();

        var brightnessSensor = new BrightnessSensor.Builder<UUID>()
                .setI2C(channel1)
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(DetectionFactory.of(UUID::randomUUID))
                .build();

        var channel2 = new I2cBuilder<UUID>(pi4j)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(Address.of(0x48))
                .setBus(I2cBus.ONE)
                .setIdentifier(UUID.randomUUID())
                .build();

        var adsConverter = new ADSConverter(channel2, new ADSConverter.Configuration(
                Address.of(0x01), Address.of(0x00), Gain.GAIN_4_096V));

        var ntcConfiguration = new TemperatureSensor.Configuration(
                Temperature.of(25), Temperature.of(50), Temperature.of(0), Resistance.of(10100),
                Resistance.of(50000), Voltage.of(3.3), 3950);

        var ntcSensor = new TemperatureSensor.Builder<UUID>()
                .setChannel(() -> adsConverter.getData(AnalogChannel.A0_IN))
                .setConfiguration(ntcConfiguration)
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(DetectionFactory.of(UUID::randomUUID))
                .build();

        var sensors = List.of(brightnessSensor, ntcSensor);

        RoomMonitoringService.of(sensors, EngineFactory.createScheduledExecutor()).start();

        pi4j.shutdown();
    }
}
