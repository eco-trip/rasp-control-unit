package io.github.ecotrip.services;

import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.Set;

public class CurrentConsumptionService<ID> extends ConsumptionService<ID, Double, Current> {
    private CurrentConsumptionService(final Set<Sensor<ID, Current>> sensors,
                                     final DetectionFactory<ID, Current> detectionFactory) {
        super(sensors, detectionFactory);
    }

    public static <ID> CurrentConsumptionService<ID> of(final DetectionFactory<ID, Current> detectionFactory) {
        return of(Set.of(), detectionFactory);
    }

    public static <ID> CurrentConsumptionService<ID> of(final Set<Sensor<ID, Current>> sensors,
                                                       final DetectionFactory<ID, Current> detectionFactory) {
        return new CurrentConsumptionService<>(sensors, detectionFactory);
    }
}
