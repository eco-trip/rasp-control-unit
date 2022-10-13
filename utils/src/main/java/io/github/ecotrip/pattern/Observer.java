package io.github.ecotrip.pattern;

/**
 * Observer for Observable pattern
 * @param <T> observer type.
 */
public interface Observer<T> {
    void notify(T value);
}
