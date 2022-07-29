package usecases;

import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.Sensor;

import java.util.concurrent.CompletableFuture;

public class EnvironmentUseCases<ID> {

    private final Sensor<ID> brightnessSensor;
    private final Sensor<ID> temperatureAndHumiditySensor;

    private EnvironmentUseCases(final Sensor<ID> temperatureAndHumiditySensor, final Sensor<ID> brightnessSensor) {
        this.brightnessSensor = brightnessSensor;
        this.temperatureAndHumiditySensor = temperatureAndHumiditySensor;
    }

    public CompletableFuture<Detection<ID>> detectRoomTemperatureAndHumidity() {
        return temperatureAndHumiditySensor.detect();
    }

    public CompletableFuture<Detection<ID>> detectRoomBrightness() {
        return brightnessSensor.detect();
    }

    public static <ID> EnvironmentUseCases<ID> of(final Sensor<ID> temperatureAndHumiditySensor,
                                                  final Sensor<ID> brightnessSensor) {
        return new EnvironmentUseCases<>(temperatureAndHumiditySensor, brightnessSensor);
    }
}
