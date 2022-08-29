package io.github.ecotrip.object;

import java.util.function.Consumer;

/**
 * Class that gathers several utils functions.
 */
public class ObjectUtils {
    /**
     * Checks if the passed object is not null and only in this case
     * it applies the consumer function.
     * @param object to be checked
     * @param consumer that implement a specific logic
     * @param <T> the object's type
     */
    public static <T> void ifNotNull(T object, Consumer<T> consumer) {
        if (object != null) {
            consumer.accept(object);
        }
    }
}
