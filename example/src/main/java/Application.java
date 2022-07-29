import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import engine.EngineFactory;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.adc.AnalogChannel;
import io.github.ecotrip.sensors.channel.I2cBus;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.services.CurrentConsumptionService;
import io.github.ecotrip.services.WaterConsumptionService;
import usecases.ConsumptionUseCases;
import usecases.EnvironmentUseCases;

import java.util.Set;
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

        var waterConsumptionService = WaterConsumptionService.of(Set.of(chy7), detectionFactory);
        var currentConsumptionService = CurrentConsumptionService.of(Set.of(acs712), detectionFactory);

        var consumptionUseCases = ConsumptionUseCases.of(waterConsumptionService, currentConsumptionService);
        var environmentUseCases = EnvironmentUseCases.of(dht22, bh1750);

        var engine = EngineFactory.createScheduledExecutor();

        RoomMonitoringService.of(engine, consumptionUseCases, environmentUseCases).start();

        pi4j.shutdown();
    }
}
