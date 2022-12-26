package io.github.ecotrip.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.Serializer;

@Generated
public class JsonSerializer<S> implements Serializer<S> {
    private final ObjectMapper objectMapper;

    private JsonSerializer(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(S element) {
        try {
            return objectMapper.writeValueAsString(element);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * factory for Serializer
     * @param <S> type
     * @param modules
     * @return a JsonSerializer
     */
    public static <S> Serializer<S> of(final Module... modules) {
        var mapper = new ObjectMapper().registerModules(modules);
        return new JsonSerializer<>(mapper);
    }
}
