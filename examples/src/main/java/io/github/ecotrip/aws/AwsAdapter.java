package io.github.ecotrip.aws;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.InputAdapter;
import io.github.ecotrip.adapter.OutputAdapter;
import io.github.ecotrip.token.Token;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.iotshadow.IotShadowClient;
import software.amazon.awssdk.iot.iotshadow.model.*;

/**
 * Amazon Web Services input and output adapter
 */
@Generated
public class AwsAdapter extends InputAdapter implements OutputAdapter<String, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsAdapter.class);
    private static final String TOKEN_PROPERTY = "token";
    private final IotShadowClient shadow;
    private final MqttClientConnection connection;
    private final String thingName;

    private AwsAdapter(final MqttClientConnection connection, final String thingName) {
        this.shadow = new IotShadowClient(connection);
        this.connection = connection;
        this.thingName = thingName;
    }

    @Override
    public CompletableFuture<Void> sendMessage(String topic, String message) {
        var msg = new MqttMessage(topic, message.getBytes(), QualityOfService.AT_LEAST_ONCE, false);
        return connection.publish(msg).thenRun(() -> {});
    }

    /**
     * connect to AWS service
     * @return connection future
     */
    public CompletableFuture<Void> connect() {
        return connection.connect()
                .thenRun(() -> LOGGER.info("AWS IoT adapter connected"))
                .thenRun(this::subscribeToGetShadow)
                .thenRun(this::subscribeToUpdateShadow);
    }

    public CompletableFuture<Void> disconnect() {
        return connection.disconnect().thenRun(() -> LOGGER.info("AWS IoT adapter disconnected"));
    }

    @Override
    public CompletableFuture<Void> requireToken() {
        var getShadowRequest = new GetShadowRequest();
        getShadowRequest.thingName = thingName;
        return shadow.PublishGetShadow(getShadowRequest, QualityOfService.AT_LEAST_ONCE)
                .thenRun(() -> LOGGER.info("Send GET/shadow request"));
    }

    private void subscribeToGetShadow() {
        var requestGetShadow = new GetShadowSubscriptionRequest();
        requestGetShadow.thingName = thingName;
        shadow.SubscribeToGetShadowAccepted(
                requestGetShadow,
                QualityOfService.AT_LEAST_ONCE,
                this::onGetShadowAccepted
        ).thenRun(() -> LOGGER.info("Subscribed to GET/shadow"));
    }

    private void subscribeToUpdateShadow() {
        var requestUpdateShadow = new UpdateShadowSubscriptionRequest();
        requestUpdateShadow.thingName = thingName;
        shadow.SubscribeToUpdateShadowAccepted(
                requestUpdateShadow,
                QualityOfService.AT_LEAST_ONCE,
                this::onUpdateShadowAccepted
        ).thenRun(() -> LOGGER.info("Subscribed to PUT/shadow"));
    }

    private void onUpdateShadowAccepted(UpdateShadowResponse response) {
        if (response.state != null) {
            handleStateResponse(response.state.reported);
        }
    }

    private void onGetShadowAccepted(final GetShadowResponse response) {
        if (response.state != null) {
            handleStateResponse(response.state.reported);
        }
    }

    private void handleStateResponse(final HashMap<String, Object> state) {
        if (state != null && state.containsKey(TOKEN_PROPERTY)) {
            var shadowToken = state.get(TOKEN_PROPERTY).toString();
            notifyObservers(Token.of(shadowToken));
            LOGGER.info("Received shadow token: " + shadowToken);
        } else {
            LOGGER.warn("Received invalid or empty token from state: " + state.toString());
        }
    }

    /**
     * AWSAdapter builder
     * @param connection MqttClientConnection
     * @param thingName thing name
     * @return the AWSAdapter
     */
    public static AwsAdapter of(final MqttClientConnection connection, final String thingName) {
        if (connection == null) {
            throw new RuntimeException("MQTT connection creation failed!");
        }
        return new AwsAdapter(connection, thingName);
    }
}
