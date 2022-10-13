package io.github.ecotrip.pattern;

import java.util.HashSet;
import java.util.Set;

/**
 * Observable pattern
 * @param <T> observer type.
 */
public class Observable<T> {
    private final Set<Observer<T>> observers;

    public Observable() {
        this.observers = new HashSet<>();
    }

    public void addObserver(Observer<T> observer) {
        observers.add(observer);
    }

    public void notifyObservers(final T value) {
        observers.forEach(o -> o.notify(value));
    }
}
