package io.github.ecotrip.services;

import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ConsumptionService<ID, M extends Measure<?>> {
    private Set<Sensor<ID, M>> sensors;
    private final DetectionFactory<ID, M> detectionFactory;

    ConsumptionService(final Set<Sensor<ID, M>> sensors,
                       final DetectionFactory<ID, M> detectionFactory) {
        this.detectionFactory = detectionFactory;
        this.sensors = sensors;
    }

    protected Set<Sensor<ID, M>> getSensors() {
        return Set.copyOf(sensors);
    }

    public void addSensors(final List<Sensor<ID, M>> toBeAdded) {
        sensors = Stream.concat(sensors.stream(), toBeAdded.stream())
                .collect(Collectors.toSet());
    }

    public void removeSensorById(final ID identifier) {
        sensors = sensors.stream().filter(s -> s.getIdentifier() != identifier)
                .collect(Collectors.toSet());
    }

    public Detection<ID, M> getConsumption() {
        final Optional<M> measure = reduceMeasures();
        return measure.isEmpty() ? detectionFactory.createEmpty() : detectionFactory.create(measure.get());
    }

    protected abstract Optional<M> reduceMeasures();
}
