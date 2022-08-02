package usecases;

import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.Sensor;
import object.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConsumptionUseCases<ID> {

    private enum SensorType {
        CURRENT, HOT_FLOW_RATE, COLD_FLOW_RATE;
    }

    private final Map<SensorType, Sensor<ID>> sensorsMap;

    private ConsumptionUseCases(final Map<SensorType, Sensor<ID>> sensorsMap) {
        this.sensorsMap = sensorsMap;
    }

    public CompletableFuture<Detection<ID>> detectCurrent() {
        return sensorsMap.get(SensorType.CURRENT).detect();
    }

    public CompletableFuture<Detection<ID>> detectHotFlowRate() {
        return sensorsMap.get(SensorType.HOT_FLOW_RATE).detect();
    }

    public CompletableFuture<Detection<ID>> detectColdFlowRate() {
        return sensorsMap.get(SensorType.COLD_FLOW_RATE).detect();
    }

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

        public ConsumptionUseCases<ID> build() {
            var sensorsMap = new HashMap<SensorType, Sensor<ID>>();
            ObjectUtils.ifNotNull(currentSensor, s -> sensorsMap.put(SensorType.CURRENT, s));
            ObjectUtils.ifNotNull(hotFlowRateSensor, s -> sensorsMap.put(SensorType.HOT_FLOW_RATE, s));
            ObjectUtils.ifNotNull(coldFlowRateSensor, s -> sensorsMap.put(SensorType.COLD_FLOW_RATE, s));
            return new ConsumptionUseCases<>(sensorsMap);
        }
    }
}