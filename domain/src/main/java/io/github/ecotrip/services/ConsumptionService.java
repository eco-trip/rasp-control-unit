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
        return combineMeasures().thenApply(m -> m.isEmpty() ? detectionFactory.createEmpty() : detectionFactory.create(List.of(m.get())));
                
    }

    private CompletableFuture<Optional<Measure>> combineMeasures() {
        Optional<CompletableFuture<List<Measure>>> measures = getSensors().stream()
                .map(Sensor::detect)
                .map(f -> f.thenApply(Detection::getMeasures))
                .reduce((f1, f2) -> f1.thenCombine(f2, this::mergeList));

        if(measures.isPresent()) { 
            return measures.get().thenApply(l -> l.stream().reduce((m1, m2) -> ((CombinableMeasure)m1).combine((CombinableMeasure)m2)));
        }
        
        return CompletableFuture.completedFuture(Optional.empty());

        // measures.thenApply(l -> l.stream().map(m -> (CombinableMeasure)m).collect(Collectors.toList()));

        //         .map(f -> f.thenApply(lm -> lm.stream().map(m -> (CombinableMeasure)m).collect(Collectors.toList())))
        //         .reduce((f1, f2) -> f1.thenCombine(f2, (l1, l2) -> Stream.of(l1, l2).flatMap(Collection::stream).collect(Collectors.toList())))
        //         .reduce((f1, f2) -> f1.thenCombine(f2, CombinableMeasure::combine));
    }

    private List<Measure> mergeList(List<Measure> l1, List<Measure> l2) {
        return Stream.concat(l1.stream(), l1.stream()).collect(Collectors.toList());
    }
}