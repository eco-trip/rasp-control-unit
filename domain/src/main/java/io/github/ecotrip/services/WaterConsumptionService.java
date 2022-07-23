package io.github.ecotrip.services;

import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.Optional;
import java.util.Set;

public class WaterConsumptionService<ID> extends ConsumptionService<ID, FlowRate> {
    private WaterConsumptionService(final Set<Sensor<ID, FlowRate>> sensors,
                                    final DetectionFactory<ID, FlowRate> detectionFactory) {
        super(sensors, detectionFactory);
    }

    @Override
    protected Optional<FlowRate> reduceMeasures() {
        return getSensors().stream().map(Sensor::detect)
                .map(Detection::getMeasure)
                .reduce((f1, f2) -> FlowRate.of(f1.getValue().add(f2.getValue())));
    }

    public static <ID> WaterConsumptionService<ID> of(final DetectionFactory<ID, FlowRate> detectionFactory) {
        return of(Set.of(), detectionFactory);
    }

    public static <ID> WaterConsumptionService<ID> of(final Set<Sensor<ID, FlowRate>> sensors,
                                                      final DetectionFactory<ID, FlowRate> detectionFactory) {
        return new WaterConsumptionService<>(sensors, detectionFactory);
    }
}
