package io.github.ecotrip.aws;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ecotrip.Generated;
import io.github.ecotrip.object.Pair;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;

/**
 * Aws helper
 */
@Generated
public class AwsHelpers {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsHelpers.class);

    /**
     * @param commands
     * @param args
     * @return
     */
    public static CommandLineUtils initCommandLineUtils(final Map<String, Pair<String, String>> commands,
                                                        final String[] args) {
        var cmdUtils = new CommandLineUtils();
        cmdUtils.registerProgramName("BasicConnect");
        cmdUtils.addCommonMqttCommands();
        cmdUtils.addCommonProxyCommands();
        commands.forEach((key, value) -> cmdUtils.registerCommand(key, value.value1(), value.value2()));
        cmdUtils.sendArguments(args);
        return cmdUtils;
    }

    /**
     * @param cmdUtils
     * @return
     */
    public static MqttClientConnection buildMqttConnection(final CommandLineUtils cmdUtils) {
        var callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    LOGGER.info("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                LOGGER.info("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
            }
        };

        try {
            return cmdUtils.buildDirectMqttConnection(callbacks);
        } catch (final Exception ex) {
            throw new RuntimeException("BasicConnect execution failure", ex);
        }
    }
}
