package io.github.ecotrip.measures.ambient;

import io.github.ecotrip.measures.Measure;

/**
 * Brightness in Lux
 */
public class Brightness extends Measure<Integer> {

    private Brightness(final int value) {
        super(value);
    }

    public static Brightness of(final int value) {
        return new Brightness(value);
    }

    @Override
    public String toString() {
        return "Brightness{" +
                "value=" + getValue() + " lux" +
                '}';
    }
}
