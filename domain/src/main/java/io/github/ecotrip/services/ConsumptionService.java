package io.github.ecotrip.services;


import io.github.ecotrip.measures.CombinableMeasure;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ConsumptionService<ID> {
    private Set<Sensor<ID>> sensors;
    private final DetectionFactory<ID> detectionFactory;

    ConsumptionService(final Set<Sensor<ID>> sensors,
                       final DetectionFactory<ID> detectionFactory) {
        this.detectionFactory = detectionFactory;
        this.sensors = sensors;
    }

    protected Set<Sensor<ID>> getSensors() {
        return Set.copyOf(sensors);
    }

    public void addSensors(final List<Sensor<ID>> toBeAdded) {
        sensors = Stream.concat(sensors.stream(), toBeAdded.stream())
                .collect(Collectors.toSet());
    }

    public void removeSensorById(final ID identifier) {
        sensors = sensors.stream().filter(s -> s.getIdentifier() != identifier)
                .collect(Collectors.toSet());
    }

    public CompletableFuture<Detection<ID>> getConsumption() {
        var measure = combineMeasures();
        return measure.isEmpty() ?
                CompletableFuture.completedFuture(detectionFactory.createEmpty()) :
                measure.get().thenApply(detectionFactory::create);
    }

    private Optional<CompletableFuture<CombinableMeasure>> combineMeasures() {
        return getSensors().stream().map(Sensor::detect)
                .map(f -> f.thenApply(Detection::getMeasure))
                .map(f -> f.thenApply(m -> (CombinableMeasure)m))
                .reduce((f1, f2) -> f1.thenCombine(f2, CombinableMeasure::combine));
    }
}
