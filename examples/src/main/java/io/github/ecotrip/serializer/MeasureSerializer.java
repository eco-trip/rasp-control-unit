package io.github.ecotrip.serializer;

import java.io.IOException;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.github.ecotrip.Generated;
import io.github.ecotrip.measure.Measure;

/**
 * Serializer for {@link Measure}
 */
@Generated
public class MeasureSerializer extends StdSerializer<Measure> {

    public MeasureSerializer() {
        this(null);
    }

    public MeasureSerializer(Class<Measure> t) {
        super(t);
    }

    @Override
    public void serialize(Measure value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        var df = new DecimalFormat("#.00");
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(value.getType().getName(), Double.parseDouble(df.format(value.getValue())));
        jsonGenerator.writeEndObject();
    }
}
