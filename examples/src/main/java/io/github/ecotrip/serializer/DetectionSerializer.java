package io.github.ecotrip.serializer;

import java.io.IOException;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.github.ecotrip.adapter.DetectionWrapper;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.MeasureType;
import io.github.ecotrip.measure.energy.Current;

/**
 * {@link DetectionSerializer} is a custom serializer for the class {@link DetectionWrapper}.
 * The serializer is used to convert the {@link DetectionWrapper} object into a JSON representation.
 */
public class DetectionSerializer extends StdSerializer<DetectionWrapper> {

    /**
     * Constructor to create the DetectionSerializer.
     * @param t Class type for DetectionWrapper.
    */
    public DetectionSerializer(Class<DetectionWrapper> t) {
        super(t);
    }

    @Override
    public void serialize(DetectionWrapper data, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        var df = new DecimalFormat("#.00");
        var detection = data.getDetection();
        jgen.writeStartObject();
        jgen.writeObjectField("identifier", detection.getIdentifier());
        jgen.writeObjectField("timestamp", detection.getDetectionTime());
        jgen.writeObjectField("sample_duration", data.getSampleDuration());
        jgen.writeObjectField("stayId", data.getStayId());
        jgen.writeObjectFieldStart("measures");
        for (Measure measure : detection.getMeasures()) {
            var measureValue = measure.getType().equals(MeasureType.CURRENT)
                    ? measure.getValue() * Current.AC_IN_VOLT : measure.getValue();
            jgen.writeNumberField(measure.getType().getName(), Double.parseDouble(df.format(measureValue)));
        }
        jgen.writeEndObject();
        jgen.writeEndObject();
    }
}
