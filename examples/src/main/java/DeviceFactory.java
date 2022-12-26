// import java.util.Arrays;
import java.util.function.Supplier;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.Pi4jProvider;
import io.github.ecotrip.adapter.adc.AnalogDigitalConverter;
import io.github.ecotrip.adapter.adc.Gain;
import io.github.ecotrip.adapter.builder.I2cBuilder;
import io.github.ecotrip.adapter.sensor.*;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Voltage;
import io.github.ecotrip.measure.water.FlowRate;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Factory of sensors
 *
 * @param <ID> uniquely identifies the sensor instance.
 */
@Generated
public class DeviceFactory<ID> {
    private final Context ctx;
    private final DetectionFactory<ID> detectionFactory;
    private final Supplier<ID> identifierGenerator;

    /**
     * DeviceFactory constructor
     * @param ctx pi4j context
     * @param detectionFactory
     * @param identifierGenerator
     */
    public DeviceFactory(final Context ctx, DetectionFactory<ID> detectionFactory, Supplier<ID> identifierGenerator) {
        this.ctx = ctx;
        this.detectionFactory = detectionFactory;
        this.identifierGenerator = identifierGenerator;
    }

    /**
     * create BH1750 sensor
     * @param pin
     * @param i2cBus
     */
    public Sensor<ID> createBH1750(final int pin, final int i2cBus) {
        var channel = new I2cBuilder<ID>(ctx)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setPin(pin)
                .setBus(i2cBus)
                .setIdentifier(identifierGenerator.get())
                .build();

        return new BH1750Sensor.Builder<ID>()
                .setI2C(channel)
                .setIdentifier(identifierGenerator.get())
                .setDetectionFactory(detectionFactory)
                .build();
    }

    /**
     * create ADS1105
     * @param pin
     * @param i2cBus
     * @return
     */
    public AnalogDigitalConverter createAds1105(final int pin, final int i2cBus) {
        var channel = new I2cBuilder<ID>(ctx)
                .setProvider(Pi4jProvider.LINUX_FS_I2C)
                .setPin(pin)
                .setBus(i2cBus)
                .setIdentifier(identifierGenerator.get())
                .build();

        return new AnalogDigitalConverter(channel, new AnalogDigitalConverter.Configuration.Builder()
                .setGain(Gain.GAIN_6_144V)
                .setConfigRegister(0x01)
                .setConversionRegister(0x00)
                .build());
    }

    /**
     * create NTC3950
     * @param channel
     * @param environment
     * @return
     */
    public Sensor<ID> createNtc3950(final Supplier<Voltage> channel,
                                    final Temperature.Environment environment) {
        var ntcConfiguration = new NtcSensor.Configuration.Builder(environment)
                .setNominalTemperature(25)
                .setMaxValue(100)
                .setMinValue(5)
                .setBoardResistance(10000)
                .setSensorResistance(50000)
                .setBvalue(3950)
                .setVcc(3.3)
                .build();

        return new NtcSensor.Builder<ID>()
                .setChannel(channel)
                .setConfiguration(ntcConfiguration)
                .setIdentifier(identifierGenerator.get())
                .setDetectionFactory(detectionFactory)
                .build();
    }

    /**
     * create CHY7
     * @param pin
     * @param flowRateType
     * @return
     */
    public Sensor<ID> createChy7(final int pin, final FlowRate.FlowRateType flowRateType) {
        var digitalInput = DigitalInput.newConfigBuilder(ctx)
                .id(identifierGenerator.get().toString())
                .pull(PullResistance.PULL_UP)
                .address(pin)
                .provider(Pi4jProvider.PIGPIO_DI.getValue())
                .build();

        return new WaterFlowHallSensor.Builder<ID>(flowRateType)
                .setFrequency(11)
                .setDigitalInput(ctx.create(digitalInput))
                .setDetectionFactory(detectionFactory)
                .setIdentifier(identifierGenerator.get())
                .build();
    }

    /**
     * create CHY7
     * @param pin
     * @return
     */
    public Sensor<ID> createDht22(final int pin) {
        var identifier = identifierGenerator.get();
        var channelConfig = DigitalMultipurpose.newConfigBuilder(ctx)
                .id(identifier.toString())
                .name("MultiDigitalChannel-" + identifier)
                .address(pin)
                .mode(DigitalMode.OUTPUT)
                .initial(DigitalState.HIGH)
                .provider(Pi4jProvider.PIGPIO_MD.getValue());

        return new DhtSensor.Builder<ID>(ctx)
                .setChannel(ctx.create(channelConfig))
                .setIdentifier(identifierGenerator.get())
                .setDetectionFactory(detectionFactory)
                .build();
    }

    /**
     * create ACS172
     * @param channel
     * @return
     */
    public Sensor<ID> createAcs172(final Supplier<Voltage> channel) {
        var acsConfiguration = new AcsSensor.Configuration.Builder()
                .setReferenceVoltage(Voltage.of(5))
                .setScaleFactor(AcsSensor.ScaleFactor.ACS_20A)
                .build();

        return new AcsSensor.Builder<ID>()
                .setChannel(channel)
                .setConfiguration(acsConfiguration)
                .setDetectionFactory(detectionFactory)
                .setIdentifier(identifierGenerator.get())
                .build();
    }
}
