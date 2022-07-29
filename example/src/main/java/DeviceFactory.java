import adapter.ADSConverter;
import adapter.Pi4jProvider;
import adapter.builder.DigitalInputBuilder;
import adapter.builder.I2cBuilder;
import adapter.builder.MultiDigitalChannelBuilder;
import adapter.sensor.*;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalMode;
import com.pi4j.io.gpio.digital.PullResistance;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.measures.energy.Resistance;
import io.github.ecotrip.measures.energy.Voltage;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;
import io.github.ecotrip.sensors.adc.AnalogChannel;
import io.github.ecotrip.sensors.adc.Gain;
import io.github.ecotrip.sensors.channel.DataChannel;
import io.github.ecotrip.sensors.channel.I2cBus;
import io.github.ecotrip.sensors.channel.MultiDigitalChannel;

import java.util.UUID;
import java.util.function.Supplier;

public class DeviceFactory<ID> {
    private final Context ctx;
    private final DetectionFactory<ID> detectionFactory;
    private final Supplier<ID> identifierGenerator;

    public DeviceFactory(final Context ctx, DetectionFactory<ID> detectionFactory, Supplier<ID> identifierGenerator) {
        this.ctx = ctx;
        this.detectionFactory = detectionFactory;
        this.identifierGenerator = identifierGenerator;
    }

    public Sensor<ID> createBH1750(final Address pin, final I2cBus i2cBus) {
        var channel = new I2cBuilder<ID>(ctx)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(pin)
                .setBus(i2cBus)
                .setIdentifier(identifierGenerator.get())
                .build();

        return new BrightnessSensor.Builder<ID>()
                .setI2C(channel)
                .setIdentifier(identifierGenerator.get())
                .setDetectionFactory(detectionFactory)
                .build();
    }

    public ADSConverter createADS1105(final Address pin, final I2cBus i2cBus) {
        var channel = new I2cBuilder<ID>(ctx)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setAddress(pin)
                .setBus(i2cBus)
                .setIdentifier(identifierGenerator.get())
                .build();

        return new ADSConverter(channel, new ADSConverter.Configuration.Builder()
                .setGain(Gain.GAIN_4_096V)
                .setConfigRegister(Address.of(0x01))
                .setConversionRegister(Address.of(0x00))
                .build());
    }

    public Sensor<ID> createNTC3950(final DataChannel<Voltage> channel) {
        var ntcConfiguration = new TemperatureSensor.Configuration.Builder()
                .setNominalTemperature(Temperature.of(25))
                .setMaxValue(Temperature.of(50))
                .setMinValue(Temperature.of(0))
                .setBoardResistance(Resistance.of(10100))
                .setSensorResistance(Resistance.of(50000))
                .setBvalue(3950)
                .setVcc(Voltage.of(3.3))
                .build();

        return new TemperatureSensor.Builder<ID>()
                .setChannel(channel)
                .setConfiguration(ntcConfiguration)
                .setIdentifier(identifierGenerator.get())
                .setDetectionFactory(detectionFactory)
                .build();
    }

    public Sensor<ID> createCHY7(final Address pin) {
        var digitalInput = new DigitalInputBuilder<ID>(ctx)
                .setAddress(pin)
                .setIdentifier(identifierGenerator.get())
                .setPullResistance(PullResistance.PULL_UP)
                .setProvider(Pi4jProvider.PIGPIO_DI)
                .build();

        return new WaterFlowSensor.Builder<ID>()
                .setFrequency(11)
                .setDigitalInput(digitalInput)
                .setDetectionFactory(detectionFactory)
                .setIdentifier(identifierGenerator.get())
                .build();
    }

    public Sensor<ID> createDHT22(final Address pin) {
        var channel = new MultiDigitalChannelBuilder<ID>(ctx)
                .setInitialState(MultiDigitalChannel.State.HIGH)
                .setMode(DigitalMode.OUTPUT)
                .setAddress(pin)
                .setIdentifier(identifierGenerator.get())
                .setProvider(Pi4jProvider.PIGPIO_MD)
                .build();

        return new TemperatureAndHumiditySensor.Builder<ID>()
                .setChannel(channel)
                .setIdentifier(identifierGenerator.get())
                .setDetectionFactory(detectionFactory)
                .build();
    }

    public Sensor<ID> createACS172(final DataChannel<Voltage> channel) {
        var acsConfiguration = new CurrentSensor.Configuration.Builder()
                .setAdcMaxValue(1023)
                .setReferenceVoltage(Voltage.of(5))
                .setScaleFactor(CurrentSensor.ScaleFactor.ACS_20A)
                .setVoltageAtZeroCurrent(Voltage.of(2.5))
                .build();

        return new CurrentSensor.Builder<ID>()
                .setChannel(channel)
                .setConfiguration(acsConfiguration)
                .setDetectionFactory(detectionFactory)
                .setIdentifier(identifierGenerator.get())
                .build();
    }
}
