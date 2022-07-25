package io.github.ecotrip.services;

import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.Set;

public class CurrentConsumptionService<ID> extends ConsumptionService<ID> {
    private CurrentConsumptionService(final Set<Sensor<ID>> sensors, final DetectionFactory<ID> detectionFactory) {
        super(sensors, detectionFactory);
    }

    public static <ID> CurrentConsumptionService<ID> of(final DetectionFactory<ID> detectionFactory) {
        return of(Set.of(), detectionFactory);
    }

    public static <ID> CurrentConsumptionService<ID> of(final Set<Sensor<ID>> sensors,
                                                        final DetectionFactory<ID> detectionFactory) {
        return new CurrentConsumptionService<>(sensors, detectionFactory);
    }
}
