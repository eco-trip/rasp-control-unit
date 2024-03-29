import static io.github.ecotrip.aws.AwsHelpers.buildMqttConnection;
import static io.github.ecotrip.aws.AwsHelpers.initCommandLineUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pi4j.Pi4J;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiChipSelect;

import io.github.ecotrip.AuthorizationService;
import io.github.ecotrip.Generated;
import io.github.ecotrip.RoomMonitoringService;
import io.github.ecotrip.adapter.AnalogChannel;
import io.github.ecotrip.adapter.DetectionWrapper;
import io.github.ecotrip.adapter.Serializer;
import io.github.ecotrip.aws.AwsAdapter;
import io.github.ecotrip.execution.engine.Engine;
import io.github.ecotrip.execution.engine.EngineFactory;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.water.FlowRate;
import io.github.ecotrip.nfc.Pn532Controller;
import io.github.ecotrip.nfc.Pn532NfcAdapter;
import io.github.ecotrip.nfc.channel.Pn532Channel;
import io.github.ecotrip.object.Pair;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.serializer.DetectionSerializer;
import io.github.ecotrip.serializer.JsonSerializer;
import io.github.ecotrip.usecase.AuthorizationUseCases;
import io.github.ecotrip.usecase.ConsumptionUseCases;
import io.github.ecotrip.usecase.EnvironmentUseCases;

@Generated
public class Application {
    private static final int I2C_BUS_ONE = 1;

    private static final Map<String, Pair<String, String>> commands = Map.of(
            "key", Pair.of("<path", "Path to your key in PEM format."),
            "cert", Pair.of("<path>", "Path to your client certificate in PEM format."),
            "client_id", Pair.of("<int>", "Client id to use (optional, default='test-*')."),
            "port", Pair.of("<int>", "Port to connect to on the endpoint (optional, default='8883').")
    );

    /**
     * main
     *
     * @param args an array of strings representing the command line arguments passed to the program
     */
    public static void main(String[] args) {
        var pi4j = Pi4J.newAutoContext();

        // Build sensors and set configurationsAWSIotTimeoutException
        var detectionFactory = DetectionFactory.of(UUID::randomUUID);
        var sensorFactory = new DeviceFactory<>(pi4j, detectionFactory, UUID::randomUUID);
        var bh1750 = sensorFactory.createBH1750(0x23, I2C_BUS_ONE);
        var ads1105 = sensorFactory.createAds1105(0x48, I2C_BUS_ONE);

        var ntc3950Hot = sensorFactory.createNtc3950(() -> ads1105.getData(AnalogChannel.A0_IN),
                Temperature.Environment.HOT_WATER_PIPE);
        var ntc3950Cold = sensorFactory.createNtc3950(() -> ads1105.getData(AnalogChannel.A1_IN),
                Temperature.Environment.COLD_WATER_PIPE);
        var acs712 = sensorFactory.createAcs172(() -> ads1105.getData(AnalogChannel.A2_IN));
        var chy7Hot = sensorFactory.createChy7(27, FlowRate.FlowRateType.HOT);
        var chy7Cold = sensorFactory.createChy7(22, FlowRate.FlowRateType.COLD);
        var dht22 = sensorFactory.createDht22(17);

        // Create use cases
        var consumptionUseCases = new ConsumptionUseCases.Builder<UUID>()
                .setHotFlowRateSensor(chy7Hot)
                .setColdFlowRateSensor(chy7Cold)
                .setCurrentSensor(acs712)
                .build();
        var environmentUseCases = new EnvironmentUseCases.Builder<UUID>()
                .setBrightnessSensor(bh1750)
                .setHotWaterTemperatureSensor(ntc3950Hot)
                .setColdWaterTemperatureSensor(ntc3950Cold)
                .setTemperatureAndHumiditySensor(dht22)
                .build();

        // Create engine to enable asynchronous programming
        Engine engine = EngineFactory.createScheduledEngine(1);

        // Create OutputAdapter using awsIotClient
        var cmdUtils = initCommandLineUtils(commands, args);
        var connection = buildMqttConnection(cmdUtils);
        var awsAdapter = AwsAdapter.of(connection, cmdUtils.getCommand("thing_name"));

        // Configure Serializer
        var javaTimeModule = new JavaTimeModule();
        var detectionModule = new SimpleModule()
                .addSerializer(DetectionWrapper.class, new DetectionSerializer(DetectionWrapper.class));

        Serializer<DetectionWrapper> serializer = JsonSerializer.of(javaTimeModule, detectionModule);

        // Create Room Monitoring Service
        var roomMonitoringService = RoomMonitoringService.of(
                engine,
                consumptionUseCases,
                environmentUseCases,
                detectionFactory,
                awsAdapter,
                serializer
        );

        // Create the second engine
        Engine engine2 = EngineFactory.createScheduledEngine(2);
        // Create NFC adapter
        var nfcChannel = Pn532Channel.createSpi(pi4j, SpiBus.BUS_0, SpiChipSelect.CS_1);
        var nfcAdapter = Pn532NfcAdapter.of(Pn532Controller.of(nfcChannel), engine2.getContext());
        // Create Authorization Service
        var authorizationUseCases = AuthorizationUseCases.of(awsAdapter, nfcAdapter);
        var authorizationService = AuthorizationService.of(engine2, authorizationUseCases);
        awsAdapter.addObserver(authorizationService);
        awsAdapter.addObserver(roomMonitoringService);

        awsAdapter.connect()
                .thenCompose(u -> CompletableFuture.allOf(roomMonitoringService.start(), authorizationService.start()))
                .exceptionally(t -> {
                    t.printStackTrace();
                    awsAdapter.disconnect();
                    pi4j.shutdown();
                    return null;
                }).join(); // main thread waits until future complete
    }
}
