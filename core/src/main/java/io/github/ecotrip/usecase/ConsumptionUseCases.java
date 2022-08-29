package io.github.ecotrip.usecase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.exception.UnassignedSensorException;
import io.github.ecotrip.object.ObjectUtils;
import io.github.ecotrip.sensor.Detection;
import io.github.ecotrip.sensor.Sensor;

/**
 * Contains all the use cases related to the measurements of room consumptions.
 * @param <ID> is the identifier's type.
 */
public class ConsumptionUseCases<ID> {

    private enum SensorType {
        CURRENT, HOT_FLOW_RATE, COLD_FLOW_RATE;
    }

    private final Map<SensorType, Sensor<ID>> sensorsMap;

    private ConsumptionUseCases(final Map<SensorType, Sensor<ID>> sensorsMap) {
        this.sensorsMap = sensorsMap;
    }

    public CompletableFuture<Detection<ID>> detectCurrent() {
        return checkSensorAndDetect(SensorType.CURRENT);
    }

    public CompletableFuture<Detection<ID>> detectHotFlowRate() {
        return checkSensorAndDetect(SensorType.HOT_FLOW_RATE);
    }

    public CompletableFuture<Detection<ID>> detectColdFlowRate() {
        return checkSensorAndDetect(SensorType.COLD_FLOW_RATE);
    }

    private CompletableFuture<Detection<ID>> checkSensorAndDetect(final SensorType sensorType) {
        return sensorsMap.containsKey(sensorType)
                ? sensorsMap.get(sensorType).detect()
                : CompletableFuture.failedFuture(new UnassignedSensorException(sensorType.name()));
    }

    /**
     * Builder used to dynamically create {@link ConsumptionUseCases} instance.
     * @param <ID> is the identifier's type.
     */
    public static class Builder<ID> {
        private Sensor<ID> currentSensor;
        private Sensor<ID> hotFlowRateSensor;
        private Sensor<ID> coldFlowRateSensor;

        public Builder<ID> setCurrentSensor(final Sensor<ID> currentSensor) {
            this.currentSensor = currentSensor;
            return this;
        }

        public Builder<ID> setHotFlowRateSensor(final Sensor<ID> hotFlowRateSensor) {
            this.hotFlowRateSensor = hotFlowRateSensor;
            return this;
        }

        public Builder<ID> setColdFlowRateSensor(final Sensor<ID> coldFlowRateSensor) {
            this.coldFlowRateSensor = coldFlowRateSensor;
            return this;
        }

        /**
         * Constructor method, following the {@link ConsumptionUseCases.Builder} pattern.
         * @return the built {@link ConsumptionUseCases} instance.
         */
        public ConsumptionUseCases<ID> build() {
            var sensorsMap = new HashMap<SensorType, Sensor<ID>>();
            ObjectUtils.ifNotNull(currentSensor, s -> sensorsMap.put(SensorType.CURRENT, s));
            ObjectUtils.ifNotNull(hotFlowRateSensor, s -> sensorsMap.put(SensorType.HOT_FLOW_RATE, s));
            ObjectUtils.ifNotNull(coldFlowRateSensor, s -> sensorsMap.put(SensorType.COLD_FLOW_RATE, s));
            return new ConsumptionUseCases<>(sensorsMap);
        }
    }
}
