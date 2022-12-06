package io.github.ecotrip.object;

/**
 * Classic Pair struct
 */
public class Pair<T, U> {
    private final T value1;
    private final U value2;

    private Pair(T value1, U value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T value1() {
        return value1;
    }

    public U value2() {
        return value2;
    }

    public static <T, U> Pair<T, U> of(final T value1, final U value2) {
        return new Pair<>(value1, value2);
    }
}
