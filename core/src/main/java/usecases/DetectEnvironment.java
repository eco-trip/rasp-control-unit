package usecases;

import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.Sensor;

import java.util.concurrent.CompletableFuture;

public class DetectEnvironment {
    public static <ID> CompletableFuture<Detection<ID>> detectRoomTemperature(Sensor<ID> sensor) {
        return sensor.detect();
    }

    public static <ID> CompletableFuture<Detection<ID>> detectRoomBrightness(Sensor<ID> sensor) {
        return sensor.detect();
    }

    public static <ID> CompletableFuture<Detection<ID>> detectRoomHumidity(Sensor<ID> sensor) {
        return sensor.detect();
    }
}
