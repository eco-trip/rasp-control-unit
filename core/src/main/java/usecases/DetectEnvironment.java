package usecases;

import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.measures.ambient.Humidity;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.Sensor;

import java.util.concurrent.CompletableFuture;

public class DetectEnvironment {
    public static <ID> CompletableFuture<Detection<ID, Temperature>> detectRoomTemperature(Sensor<ID, Temperature> sensor) {
        return sensor.detect();
    }

    public static <ID> CompletableFuture<Detection<ID, Brightness>> detectRoomBrightness(Sensor<ID, Brightness> sensor) {
        return sensor.detect();
    }

    public static <ID> CompletableFuture<Detection<ID, Humidity>> detectRoomHumidity(Sensor<ID, Humidity> sensor) {
        return sensor.detect();
    }
}
