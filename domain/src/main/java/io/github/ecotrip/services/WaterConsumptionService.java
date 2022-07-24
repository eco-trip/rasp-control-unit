package io.github.ecotrip.services;

import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.measures.water.Liter;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.Set;

public class WaterConsumptionService<ID> extends ConsumptionService<ID, Liter, FlowRate> {
    private WaterConsumptionService(final Set<Sensor<ID, FlowRate>> sensors,
                                    final DetectionFactory<ID, FlowRate> detectionFactory) {
        super(sensors, detectionFactory);
    }

    public static <ID> WaterConsumptionService<ID> of(final DetectionFactory<ID, FlowRate> detectionFactory) {
        return of(Set.of(), detectionFactory);
    }

    public static <ID> WaterConsumptionService<ID> of(final Set<Sensor<ID, FlowRate>> sensors,
                                                      final DetectionFactory<ID, FlowRate> detectionFactory) {
        return new WaterConsumptionService<>(sensors, detectionFactory);
    }
}
