package io.github.ecotrip.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.github.ecotrip.sensor.Detection;

/**
 * Serializer for {@link Detection}
 */
public class DetectionSerializer extends StdSerializer<Detection> {
    public DetectionSerializer() {
        this(null);
    }

    public DetectionSerializer(Class<Detection> t) {
        super(t);
    }

    @Override
    public void serialize(Detection value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeObjectField("identifier", value.getIdentifier());
        jgen.writeObjectField("timestamp", value.getDetectionTime());
        jgen.writeObjectField("processed", 0);
        jgen.writeObjectFieldStart("measures");
        for (Object measure : value.getMeasures()) {
            provider.defaultSerializeValue(measure, jgen);
        }
        jgen.writeEndObject();
        jgen.writeEndObject();
    }
}
