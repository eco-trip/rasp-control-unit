package io.github.ecotrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.ecotrip.measure.IncompatibleMeasuresException;
import io.github.ecotrip.measure.MeasureType;
import io.github.ecotrip.measure.ambient.Brightness;
import io.github.ecotrip.measure.ambient.Humidity;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.measure.energy.Resistance;
import io.github.ecotrip.measure.energy.Voltage;
import io.github.ecotrip.measure.water.FlowRate;

public class MeasureTest {
    @Test
    public void testBrightnessByEquals() {
        var b1 = Brightness.of(300);
        var b2 = Brightness.of(300);
        var b3 = Brightness.of(100);
        assertEquals(b1, b2);
        assertEquals(b1, b1);
        assertNotEquals(b1, b3);
        assertNotEquals(b1.hashCode(), b3.hashCode());
    }

    @Test
    public void testBrightnessOps() {
        var b1 = Brightness.of(300);
        var b2 = Brightness.of(300);
        var b3 = Brightness.of(100);
        assertTrue(b1.isGreaterEqualThan(b2));
        assertTrue(b2.isGreaterEqualThan(b3));
        assertTrue(b3.isLessEqualThan(b2));
        assertFalse(b3.isGreaterEqualThan(b1));
        assertEquals(b1.toString(), "Brightness{value=" + b1.getValue() + " lux}");
    }

    @Test
    public void testHumidityByEquals() {
        var h1 = Humidity.of(55);
        var h2 = Humidity.of(55);
        var h3 = Humidity.of(10);
        assertEquals(h1, h2);
        assertEquals(h1, h1);
        assertNotEquals(h1, h3);
    }

    @Test
    public void testHumidityOps() {
        var h1 = Humidity.of(55);
        var h2 = Humidity.of(55);
        var h3 = Humidity.of(10);
        assertTrue(h1.isGreaterEqualThan(h2));
        assertTrue(h2.isGreaterEqualThan(h3));
        assertTrue(h3.isLessEqualThan(h1));
        assertFalse(h3.isGreaterEqualThan(h1));
        assertEquals(h1.toString(), "Humidity{value=" + h1.getValue() + "%}");
    }

    @Test
    public void testTemperatureByEquals() {
        var coldWaterPipe = Temperature.Environment.COLD_WATER_PIPE;
        final Temperature t1 = Temperature.of(25, coldWaterPipe);
        final Temperature t2 = Temperature.of(25, coldWaterPipe);
        final Temperature t3 = Temperature.of(12, coldWaterPipe);
        assertEquals(t1, t2);
        assertEquals(t1, t1);
        assertNotEquals(t1, t3);
        assertFalse(t1.equals(Brightness.of(30)));
    }

    @Test
    public void testTemperatureOps() {
        var coldWaterPipe = Temperature.Environment.COLD_WATER_PIPE;
        final Temperature t1 = Temperature.of(25, coldWaterPipe);
        final Temperature t2 = Temperature.of(25, coldWaterPipe);
        final Temperature t3 = Temperature.of(12, coldWaterPipe);
        assertTrue(t1.isGreaterEqualThan(t2));
        assertTrue(t2.isGreaterEqualThan(t3));
        assertTrue(t3.isLessEqualThan(t2));
        assertFalse(t1.isLessEqualThan(t3));
        assertFalse(t3.isGreaterEqualThan(t1));
    }

    @Test
    public void testTemperatureEnvironment() {
        var env1 = Temperature.Environment.ROOM;
        var env2 = Temperature.Environment.HOT_WATER_PIPE;
        var env3 = Temperature.Environment.COLD_WATER_PIPE;
        var roomTemp = Temperature.of(20, env1);
        var hotWaterTemp = Temperature.of(50, env2);
        var coldWaterTemp = Temperature.of(10, env3);
        assertNotEquals(roomTemp, hotWaterTemp);
        assertNotEquals(roomTemp, coldWaterTemp);
        assertEquals(roomTemp, Temperature.of(20, env1));
        assertNotEquals(coldWaterTemp, hotWaterTemp);
        assertEquals(roomTemp.getType(), MeasureType.ROOM_TEMPERATURE);
        assertEquals(coldWaterTemp.getType(), MeasureType.COLD_WATER_TEMPERATURE);
        assertEquals(hotWaterTemp.getType(), MeasureType.HOT_WATER_TEMPERATURE);
        assertThrows(IncompatibleMeasuresException.class, () -> roomTemp.isLessEqualThan(hotWaterTemp));
    }

    @Test
    public void testCurrentByEquals() {
        var c1 = Current.of(3.3);
        var c2 = Current.of(3.3);
        var c3 = Current.of(5);
        assertEquals(c1, c2);
        assertEquals(c1, c1);
        assertNotEquals(c1, c3);
        assertEquals(c1, c2);
    }

    @Test
    public void testCurrentOps() {
        var c1 = Current.of(3.3);
        var c2 = Current.of(3.3);
        var c3 = Current.of(5);
        assertTrue(c1.isGreaterEqualThan(c2));
        assertFalse(c2.isGreaterEqualThan(c3));
        assertFalse(c3.isLessEqualThan(c1));
        assertTrue(c3.isGreaterEqualThan(c1));
        assertEquals(c1.checkAndCombine(c2), Current.of(6.6));
        assertEquals(c1.toString(), "Current{value=" + c1.getValue() + " amps}");
        assertEquals(Current.of(Voltage.of(1500), Resistance.of(100)), Current.of(15));
    }

    @Test
    public void testResistanceByEquals() {
        var r1 = Resistance.of(5);
        var r2 = Resistance.of(5);
        var r3 = Resistance.of(3);
        assertEquals(r1, r2);
        assertEquals(r1, r1);
        assertNotEquals(r1, r3);
    }

    @Test
    public void testResistanceOps() {
        var r1 = Resistance.of(5);
        var r2 = Resistance.of(5);
        var r3 = Resistance.of(3);
        assertTrue(r1.isGreaterEqualThan(r3));
        assertTrue(r1.isGreaterEqualThan(r2));
        assertTrue(r3.isLessEqualThan(r1));
        assertFalse(r3.isGreaterEqualThan(r1));
        assertThrows(IncompatibleMeasuresException.class, () -> r1.isGreaterEqualThan(Current.of(5)));
        assertEquals(r1.toString(), "Resistance{value=" + r1.getValue() + " ohm}");
        assertEquals(Resistance.of(Current.of(15), Voltage.of(1500)), Resistance.of(100));
    }

    @Test
    public void testVoltageByEquals() {
        var v1 = Voltage.of(15);
        var v2 = Voltage.of(15);
        var v3 = Voltage.of(3);
        assertEquals(v1, v2);
        assertEquals(v1, v1);
        assertNotEquals(v1, v3);
    }

    @Test
    public void testVoltageOps() {
        var v1 = Voltage.of(15);
        var v2 = Voltage.of(3);
        assertEquals(v1.checkAndCombine(v2), Voltage.of(18));
        assertThrows(IncompatibleMeasuresException.class, () -> v1.checkAndCombine(Current.of(10)));
        assertEquals(v1.toString(), "Voltage{value=" + v1.getValue() + " volts}");
        assertEquals(Voltage.of(Current.of(15), Resistance.of(100)), Voltage.of(1500));
    }

    @Test
    public void testFlowRateByEquals() {
        var fr1 = FlowRate.of(3, FlowRate.FlowRateType.HOT);
        var fr2 = FlowRate.of(3, FlowRate.FlowRateType.HOT);
        var fr3 = FlowRate.of(3, FlowRate.FlowRateType.COLD);
        var fr4 = FlowRate.of(2, FlowRate.FlowRateType.HOT);

        assertEquals(fr1, fr2);
        assertNotEquals(fr1, fr3);
        assertNotEquals(fr1, fr4);
        assertEquals(fr1, fr1);
    }

    @Test
    public void testFlowRateOps() {
        var fr1 = FlowRate.of(40, FlowRate.FlowRateType.HOT);
        var fr2 = FlowRate.of(50, FlowRate.FlowRateType.HOT);
        var fr3 = FlowRate.of(3, FlowRate.FlowRateType.COLD);
        var fr4 = FlowRate.of(5, FlowRate.FlowRateType.COLD);

        assertEquals(fr1.checkAndCombine(fr2), FlowRate.of(90, FlowRate.FlowRateType.HOT));
        assertEquals(fr3.checkAndCombine(fr4), FlowRate.of(8, FlowRate.FlowRateType.COLD));
        assertThrows(IncompatibleMeasuresException.class, () -> fr2.checkAndCombine(fr3));
        assertEquals(fr1.toString(), "FlowRate{value=" + fr1.getValue() + " liters/min}");
    }
}
