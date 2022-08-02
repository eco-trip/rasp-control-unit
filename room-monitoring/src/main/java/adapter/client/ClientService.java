package adapter.client;

public interface ClientService<T, M> {
    void start();

    void sendMessage(final M message);

    void sendMessage(final T topic, final M message);
}
