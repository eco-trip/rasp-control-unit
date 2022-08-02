import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import engine.Engine;
import engine.EngineFactory;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.adc.AnalogChannel;
import io.github.ecotrip.sensors.channel.I2cBus;
import io.github.ecotrip.sensors.DetectionFactory;
import usecases.ConsumptionUseCases;
import usecases.EnvironmentUseCases;

import java.util.UUID;

public class Application {
    public static void main(String[] args) {
        final Console console = new Console();

        console.title("<-- The Pi4J Project -->", "Rasp Control Unit System");

        var pi4j = Pi4J.newAutoContext();

        var detectionFactory = DetectionFactory.of(UUID::randomUUID);
        var sensorFactory = new DeviceFactory<>(pi4j, detectionFactory , UUID::randomUUID);

        var bh1750 = sensorFactory.createBH1750(Address.of(0x23), I2cBus.ONE);
        var ads1105 = sensorFactory.createADS1105(Address.of(0x48), I2cBus.ONE);
        var ntc3950 = sensorFactory.createNTC3950(() -> ads1105.getData(AnalogChannel.A0_IN));
        var acs712 = sensorFactory.createACS172(() -> ads1105.getData(AnalogChannel.A1_IN));
        var chy7 = sensorFactory.createCHY7(Address.of(27));
        var dht22 = sensorFactory.createDHT22(Address.of(17));

        var consumptionUseCases = new ConsumptionUseCases.Builder<UUID>()
                .setHotFlowRateSensor(chy7)
                .setCurrentSensor(acs712)
                .build();

        var environmentUseCases = new EnvironmentUseCases.Builder<UUID>()
                .setBrightnessSensor(bh1750)
                .setHotWaterTemperatureSensor(ntc3950)
                .setTemperatureAndHumiditySensor(dht22)
                .build();

        final Engine<Detection<UUID>> engine = EngineFactory.createScheduledExecutor();

        RoomMonitoringService.of(engine, consumptionUseCases, environmentUseCases, detectionFactory).start();

        pi4j.shutdown();
    }
}
