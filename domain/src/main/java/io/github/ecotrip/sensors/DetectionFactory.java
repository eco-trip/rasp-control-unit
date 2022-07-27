package io.github.ecotrip.sensors;

import io.github.ecotrip.measures.Measure;

import java.util.List;
import java.util.function.Supplier;

public class DetectionFactory<ID> {
    private final Supplier<ID> idGenerator;

    private DetectionFactory(final Supplier<ID> idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Detection<ID> create(final List<Measure> measure) {
        return Detection.of(idGenerator.get(), measure);
    }

    public Detection<ID> createEmpty() {
        return Detection.empty(idGenerator.get());
    }

    public static <ID> DetectionFactory<ID> of(final Supplier<ID> idGenerator) {
        return new DetectionFactory<>(idGenerator);
    }
}
