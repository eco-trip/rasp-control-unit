package io.github.ecotrip.energy;

import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.energy.Resistance;
import io.github.ecotrip.measures.energy.Voltage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class VoltageTest {
    final Voltage dummyVoltage = Voltage.of(5);

    @Test
    public void testCreation() {
        final Voltage voltage = Voltage.of(Current.of(0.5), Resistance.of(2));
        assertEquals(voltage, Voltage.of(1.0));
        assertNotEquals(voltage, Voltage.of(0.0));
    }
}
