import adapter.*;
import adapter.builder.DigitalInputBuilder;
import adapter.builder.I2cBuilder;
import adapter.builder.MultiDigitalChannelBuilder;
import adapter.sensor.*;
import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalMode;
import com.pi4j.io.gpio.digital.PullResistance;
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
import io.github.ecotrip.sensors.channel.MultiDigitalChannel;

import java.util.List;
import java.util.UUID;

public class Application {
    public static void main(String[] args) {
        final Console console = new Console();

        console.title("<-- The Pi4J Project -->", "Rasp Control Unit System");

        var pi4j = Pi4J.newAutoContext();

        var detectionFactory = DetectionFactory.of(UUID::randomUUID);

        var channel1 = new I2cBuilder<UUID>(pi4j)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(Address.of(0x23))
                .setBus(I2cBus.ONE)
                .setIdentifier(UUID.randomUUID())
                .build();

        var bh1750 = new BrightnessSensor.Builder<UUID>()
                .setI2C(channel1)
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(detectionFactory)
                .build();

        var channel2 = new I2cBuilder<UUID>(pi4j)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(Address.of(0x48))
                .setBus(I2cBus.ONE)
                .setIdentifier(UUID.randomUUID())
                .build();

        var adsConverter = new ADSConverter(channel2, new ADSConverter.Configuration.Builder()
                .setGain(Gain.GAIN_4_096V)
                .setConfigRegister(Address.of(0x01))
                .setConversionRegister(Address.of(0x00))
                .build());

        var ntcConfiguration = new TemperatureSensor.Configuration.Builder()
                .setNominalTemperature(Temperature.of(25))
                .setMaxValue(Temperature.of(50))
                .setMinValue(Temperature.of(0))
                .setBoardResistance(Resistance.of(10100))
                .setSensorResistance(Resistance.of(50000))
                .setBvalue(3950)
                .setVcc(Voltage.of(3.3))
                .build();

        var ntc3950 = new TemperatureSensor.Builder<UUID>()
                .setChannel(() -> adsConverter.getData(AnalogChannel.A0_IN))
                .setConfiguration(ntcConfiguration)
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(detectionFactory)
                .build();

        var digitalInput = new DigitalInputBuilder<UUID>(pi4j)
                .setAddress(Address.of(27))
                .setIdentifier(UUID.randomUUID())
                .setPullResistance(PullResistance.PULL_UP)
                .setProvider(Pi4jProvider.PIGPIO_DI)
                .build();

        var chy7 = new WaterFlowSensor.Builder<UUID>()
                .setFrequency(11)
                .setDigitalInput(digitalInput)
                .setDetectionFactory(detectionFactory)
                .setIdentifier(UUID.randomUUID())
                .build();

        var channel3 = new MultiDigitalChannelBuilder<UUID>(pi4j)
                .setInitialState(MultiDigitalChannel.State.HIGH)
                .setMode(DigitalMode.OUTPUT)
                .setAddress(Address.of(17))
                .setIdentifier(UUID.randomUUID())
                .setProvider(Pi4jProvider.PIGPIO_MD)
                .build();

        var dht22 = new TemperatureAndHumiditySensor.Builder<UUID>()
                .setChannel(channel3)
                .setIdentifier(UUID.randomUUID())
                .setDetectionFactory(detectionFactory)
                .build();

        var acsConfiguration = new CurrentSensor.Configuration.Builder()
                .setAdcMaxValue(1023)
                .setReferenceVoltage(Voltage.of(5))
                .setScaleFactor(CurrentSensor.ScaleFactor.ACS_20A)
                .setVoltageAtZeroCurrent(Voltage.of(2.5))
                .build();

        var acs712 = new CurrentSensor.Builder<UUID>()
                .setChannel(() -> adsConverter.getData(AnalogChannel.A1_IN))
                .setConfiguration(acsConfiguration)
                .setDetectionFactory(detectionFactory)
                .setIdentifier(UUID.randomUUID())
                .build();

        var sensors = List.of(acs712, chy7, dht22, ntc3950, bh1750);

        RoomMonitoringService.of(sensors, EngineFactory.createScheduledExecutor()).start();

        pi4j.shutdown();
    }
}
