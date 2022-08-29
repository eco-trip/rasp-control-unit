package io.github.ecotrip.usecase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.exception.UnassignedSensorException;
import io.github.ecotrip.object.ObjectUtils;
import io.github.ecotrip.sensor.Detection;
import io.github.ecotrip.sensor.Sensor;

/**
 * Contains all the use cases related to the measurements
 * of environmental factors (e.g room temperature).
 * @param <ID> is the identifier's type.
 */
public class EnvironmentUseCases<ID> {

    private enum SensorType {
        TEMP_AND_HUMIDITY, COLD_WATER_TEMP, HOT_WATER_TEMP, BRIGHTNESS;
    }

    private final Map<SensorType, Sensor<ID>> sensorsMap;

    private EnvironmentUseCases(final Map<SensorType, Sensor<ID>> sensorsMap) {
        this.sensorsMap = sensorsMap;
    }

    public CompletableFuture<Detection<ID>> detectRoomTemperatureAndHumidity() {
        return checkSensorAndDetect(SensorType.TEMP_AND_HUMIDITY);
    }

    public CompletableFuture<Detection<ID>> detectRoomBrightness() {
        return checkSensorAndDetect(SensorType.BRIGHTNESS);
    }

    public CompletableFuture<Detection<ID>> detectColdWaterTemperature() {
        return checkSensorAndDetect(SensorType.COLD_WATER_TEMP);
    }

    public CompletableFuture<Detection<ID>> detectHotWaterTemperature() {
        return checkSensorAndDetect(SensorType.HOT_WATER_TEMP);
    }

    private CompletableFuture<Detection<ID>> checkSensorAndDetect(final SensorType sensorType) {
        return sensorsMap.containsKey(sensorType)
                ? sensorsMap.get(sensorType).detect()
                : CompletableFuture.failedFuture(new UnassignedSensorException(sensorType.name()));
    }

    /**
     * Builder used to dynamically create an {@link EnvironmentUseCases} instance.
     * @param <ID> is the identifier's type.
     */
    public static class Builder<ID> {
        private Sensor<ID> brightnessSensor;
        private Sensor<ID> temperatureAndHumiditySensor;
        private Sensor<ID> coldWaterTemperatureSensor;
        private Sensor<ID> hotWaterTemperatureSensor;

        public Builder<ID> setColdWaterTemperatureSensor(final Sensor<ID> coldWaterTemperatureSensor) {
            this.coldWaterTemperatureSensor = coldWaterTemperatureSensor;
            return this;
        }

        public Builder<ID> setHotWaterTemperatureSensor(final Sensor<ID> hotWaterTemperatureSensor) {
            this.hotWaterTemperatureSensor = hotWaterTemperatureSensor;
            return this;
        }

        public Builder<ID> setTemperatureAndHumiditySensor(final Sensor<ID> temperatureAndHumiditySensor) {
            this.temperatureAndHumiditySensor = temperatureAndHumiditySensor;
            return this;
        }

        public Builder<ID> setBrightnessSensor(final Sensor<ID> brightnessSensor) {
            this.brightnessSensor = brightnessSensor;
            return this;
        }

        /**
         * Constructor method, following the {@link Builder} pattern.
         * @return the built {@link EnvironmentUseCases} instance.
         */
        public EnvironmentUseCases<ID> build() {
            var sensorsMap = new HashMap<SensorType, Sensor<ID>>();
            ObjectUtils.ifNotNull(temperatureAndHumiditySensor, s -> sensorsMap.put(SensorType.TEMP_AND_HUMIDITY, s));
            ObjectUtils.ifNotNull(brightnessSensor, s -> sensorsMap.put(SensorType.BRIGHTNESS, s));
            ObjectUtils.ifNotNull(coldWaterTemperatureSensor, s -> sensorsMap.put(SensorType.COLD_WATER_TEMP, s));
            ObjectUtils.ifNotNull(hotWaterTemperatureSensor, s -> sensorsMap.put(SensorType.HOT_WATER_TEMP, s));
            return new EnvironmentUseCases<>(sensorsMap);
        }
    }
}
