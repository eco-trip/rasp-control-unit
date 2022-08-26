package io.github.ecotrip.measure;

/**
 * Enum representing all measure types
 */
public enum MeasureType {
    ROOM_TEMPERATURE("room_temperature"),
    HUMIDITY("humidity"),
    BRIGHTNESS("brightness"),
    HOT_WATER_TEMPERATURE("hot_water_temperature"),
    COLD_WATER_TEMPERATURE("cold_water_temperature"),
    HOT_FLOW_RATE("hot_flow_rate"),
    COLD_FLOW_RATE("cold_flow_rate"),
    CURRENT("current"),
    VOLTAGE("voltage"),
    RESISTANCE("resistance");

    private final String name;

    MeasureType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
