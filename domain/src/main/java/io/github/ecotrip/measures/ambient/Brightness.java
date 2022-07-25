package io.github.ecotrip.measures.ambient;

import io.github.ecotrip.measures.Measure;

/**
 * Brightness in Lux
 */
public class Brightness extends Measure {

    private Brightness(final double value) {
        super(value);
    }

    public static Brightness of(final double value) {
        return new Brightness(value);
    }

    @Override
    public String toString() {
        return "Brightness{" +
                "value=" + getValue() + " lux" +
                '}';
    }
}
