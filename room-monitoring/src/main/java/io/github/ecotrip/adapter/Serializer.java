package io.github.ecotrip.adapter;

/**
 * Abstract serializer contract.
 * @param <S> is the type of the object to serialize.
 */
public interface Serializer<S> {
    String serialize(S element);
}
