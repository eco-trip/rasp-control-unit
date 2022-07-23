package io.github.ecotrip.sensors;

import io.github.ecotrip.measures.Measure;

import java.util.function.Supplier;

public class DetectionFactory<ID, M extends Measure<?>> {
    private final Supplier<ID> idGenerator;

    private DetectionFactory(final Supplier<ID> idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Detection<ID, M> create(final M measure) {
        return Detection.of(idGenerator.get(), measure);
    }

    public Detection<ID, M> createEmpty() {
        return Detection.empty(idGenerator.get());
    }

    public static <ID, M extends Measure<?>> DetectionFactory<ID, M> of(final Supplier<ID> idGenerator) {
        return new DetectionFactory<>(idGenerator);
    }
}
