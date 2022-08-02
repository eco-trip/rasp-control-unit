package usecases;

import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.Sensor;
import object.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EnvironmentUseCases<ID> {

    private enum SensorType {
        TEMP_AND_HUMIDITY, COLD_WATER_TEMP, HOT_WATER_TEMP, BRIGHTNESS;
    }

    private final Map<SensorType, Sensor<ID>> sensorsMap;

    private EnvironmentUseCases(final Map<SensorType, Sensor<ID>> sensorsMap) {
        this.sensorsMap = sensorsMap;
    }

    public CompletableFuture<Detection<ID>> detectRoomTemperatureAndHumidity() {
        return sensorsMap.get(SensorType.TEMP_AND_HUMIDITY).detect();
    }

    public CompletableFuture<Detection<ID>> detectRoomBrightness() {
        return sensorsMap.get(SensorType.BRIGHTNESS).detect();
    }

    public CompletableFuture<Detection<ID>> detectColdWaterTemperature() {
        return sensorsMap.get(SensorType.HOT_WATER_TEMP).detect();
    }

    public CompletableFuture<Detection<ID>> detectHotWaterTemperature() {
        return sensorsMap.get(SensorType.HOT_WATER_TEMP).detect();
    }

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
