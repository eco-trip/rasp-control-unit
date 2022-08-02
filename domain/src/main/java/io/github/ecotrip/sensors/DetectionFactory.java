package io.github.ecotrip.sensors;

import io.github.ecotrip.measures.Measure;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DetectionFactory<ID> {
    private final Supplier<ID> idGenerator;

    private DetectionFactory(final Supplier<ID> idGenerator) {
        this.idGenerator = idGenerator;
    }

    public synchronized Detection<ID> create(final List<Measure> measure) {
        return Detection.of(idGenerator.get(), measure);
    }

    public Detection<ID> merge(Detection<ID> d1, Detection<ID> d2) {
        var measures = Stream.concat(d1.getMeasures().stream(), d2.getMeasures().stream())
                .collect(Collectors.toList());
        return create(measures);
    }

    public synchronized Detection<ID> createEmpty() {
        return Detection.empty(idGenerator.get());
    }

    public static <ID> DetectionFactory<ID> of(final Supplier<ID> idGenerator) {
        return new DetectionFactory<>(idGenerator);
    }
}
