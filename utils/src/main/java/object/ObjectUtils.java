package object;

import java.util.function.Consumer;

public class ObjectUtils {
    public static <T> void ifNotNull(T object, Consumer<T> consumer) {
        if(object != null) consumer.accept(object);
    }
}
