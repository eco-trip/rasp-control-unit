package io.github.ecotrip.services;

import io.github.ecotrip.measures.CombinableMeasure;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ConsumptionService<ID, T, M extends CombinableMeasure<T>> {
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

    public CompletableFuture<Detection<ID, M>> getConsumption() {
        var measure = combineMeasures();
        return measure.isEmpty() ?
                CompletableFuture.completedFuture(detectionFactory.createEmpty()) :
                measure.get().thenApply(detectionFactory::create);
    }

    private Optional<CompletableFuture<M>> combineMeasures() {
        return getSensors().stream().map(Sensor::detect)
                .map(f -> f.thenApply(Detection::getMeasure))
                .reduce((f1, f2) -> f1.thenCombine(f2, (c1, c2) -> (M) c1.combine(c2)));
    }
}
