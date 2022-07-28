package io.github.ecotrip.energy;

import io.github.ecotrip.measures.InvalidMeasureException;
import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.measures.ambient.Humidity;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.energy.Resistance;
import io.github.ecotrip.measures.energy.Voltage;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.measures.water.Liter;
import io.github.ecotrip.sensors.SensorOutOfRangeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureTest {

    @Test
    public void testResistanceByEquals() {
        final Resistance r1 = Resistance.of(5);
        assertEquals(r1, Resistance.of(5));
        assertEquals(r1, r1);
        assertNotEquals(r1, Resistance.of(3));
    }

    @Test
    public void testBrightnessByEquals() {
        final Brightness b1 = Brightness.of(300);
        assertEquals(b1, Brightness.of(300));
        assertEquals(b1, b1);
        assertNotEquals(b1, Brightness.of(100));
    }

    @Test
    public void testHumidityByEquals() throws InvalidMeasureException {
        final Humidity h1 = Humidity.of(55);
        assertEquals(h1, Humidity.of(55));
        assertEquals(h1, h1);
        assertNotEquals(h1, Humidity.of(10));
        assertThrows(SensorOutOfRangeException.class, () -> Humidity.of(-10));
        assertThrows(SensorOutOfRangeException.class, () -> Humidity.of(101));
    }

    @Test
    public void testTemperatureByEquals() {
        final Temperature t1 = Temperature.of(25);
        assertEquals(t1, Temperature.of(25));
        assertEquals(t1, t1);
        assertNotEquals(t1, Temperature.of(13));
    }

    @Test
    public void testCurrentByEquals() {
        final Current c1 = Current.of(3.3);
        assertEquals(c1, Current.of(3.3));
        assertEquals(c1, c1);
        assertNotEquals(c1, Current.of(5));
    }

    @Test
    public void testVoltageByEquals() {
        final Voltage v1 = Voltage.of(15);
        assertEquals(v1, Voltage.of(15));
        assertEquals(v1, v1);
        assertNotEquals(v1, Voltage.of(3));
    }
}
