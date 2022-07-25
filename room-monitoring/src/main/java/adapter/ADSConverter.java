package adapter;

import com.pi4j.io.i2c.I2C;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.adc.Converter;
import io.github.ecotrip.sensors.adc.Gain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ADSConverter extends Converter {
    private static final Logger LOG = LoggerFactory.getLogger(ADSConverter.class);
    private final I2C channel;

    public ADSConverter(I2C channel, Configuration configuration) {
        super(configuration.gain, configuration.configRegister, configuration.conversionRegister);
        this.channel = channel;
    }

    @Override
    protected CompletableFuture<Integer> readIn(int config) {
        return CompletableFuture.supplyAsync(() -> {
            channel.writeRegisterWord(getConfigRegister().getValue(), config);
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
            return channel.readRegisterWord(getConversionRegister().getValue());
        });
    }

    public static class Configuration {
        private final Address configRegister;
        private final Address conversionRegister;
        private final Gain gain;

        public Configuration(Address configRegister, Address conversionRegister, Gain gain) {
            this.configRegister = configRegister;
            this.conversionRegister = conversionRegister;
            this.gain = gain;
        }
    }
}