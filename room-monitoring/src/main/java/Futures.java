import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Futures {
    public static <T> CompletableFuture<Void> mergeFutures(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static <T, U> CompletableFuture<U> thenAll(List<CompletableFuture<T>> futures, Function<List<T>, U> fn) {
        return mergeFutures(futures).thenApply(unused -> {
            List<T> results = futures.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList());
            return fn.apply(results);
        });
    }
}
