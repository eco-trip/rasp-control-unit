package io.github.ecotrip.services;

import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.Set;

public class WaterConsumptionService<ID> extends ConsumptionService<ID> {
    private WaterConsumptionService(final Set<Sensor<ID>> sensors,
                                    final DetectionFactory<ID> detectionFactory) {
        super(sensors, detectionFactory);
    }

    public static <ID> WaterConsumptionService<ID> of(final DetectionFactory<ID> detectionFactory) {
        return of(Set.of(), detectionFactory);
    }

    public static <ID> WaterConsumptionService<ID> of(final Set<Sensor<ID>> sensors,
                                                      final DetectionFactory<ID> detectionFactory) {
        return new WaterConsumptionService<>(sensors, detectionFactory);
    }
}
