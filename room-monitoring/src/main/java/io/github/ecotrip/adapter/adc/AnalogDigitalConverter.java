package io.github.ecotrip.adapter.adc;

import com.pi4j.io.i2c.I2C;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.AnalogChannel;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.measure.energy.Voltage;

/**
 * ADC multichannel
 */
@Generated
public class AnalogDigitalConverter {
    private static final int INTERVAL_READ_WRITE = 800;
    private final I2C channel;
    private final Configuration configuration;

    /**
     * ADC constructor
     * @param channel PI channel interface
     * @param configuration ADC work configuration
     */
    public AnalogDigitalConverter(I2C channel, Configuration configuration) {
        this.configuration = configuration;
        this.channel = channel;
    }

    public synchronized Voltage getData(final AnalogChannel channel) {
        var rawData = readIn(calculateConfig(channel.getId()));
        return Voltage.of(rawData * configuration.gain.getValuePerByte());
    }

    /**
     * Blocking method to retrive raw data from the converter
     * @param config includes the channel used to get the data
     * @return the raw value
     */
    private int readIn(int config) {
        channel.writeRegisterWord(configuration.configRegister, config);
        Execution.delayMicroseconds(INTERVAL_READ_WRITE);
        return channel.readRegisterWord(configuration.conversionRegister);
    }

    private int calculateConfig(int pinId) {
        return configuration.template | configuration.gain.getValue() | pinId;
    }

    /**
     * ADC Configuration
     */
    public static class Configuration {
        private final int configRegister;
        private final int conversionRegister;
        private final Gain gain;
        private final int template;

        private Configuration(int configRegister, int conversionRegister, Gain gain, int template) {
            this.configRegister = configRegister;
            this.conversionRegister = conversionRegister;
            this.gain = gain;
            this.template = template;
        }

        /**
         * Builder
         */
        public static class Builder {
            private static final int DEFAULT_CONFIG_REGISTER_TEMPLATE = 0b1000000111100011;
            private int configRegister;
            private int conversionRegister;
            private Gain gain;
            private int template = DEFAULT_CONFIG_REGISTER_TEMPLATE;

            public Builder setConfigRegister(int configRegister) {
                this.configRegister = configRegister;
                return this;
            }

            public Builder setConversionRegister(int conversionRegister) {
                this.conversionRegister = conversionRegister;
                return this;
            }

            public Builder setGain(Gain gain) {
                this.gain = gain;
                return this;
            }

            public Builder setTemplate(int template) {
                this.template = template;
                return this;
            }

            public Configuration build() {
                return new Configuration(configRegister, conversionRegister, gain, template);
            }
        }
    }
}
