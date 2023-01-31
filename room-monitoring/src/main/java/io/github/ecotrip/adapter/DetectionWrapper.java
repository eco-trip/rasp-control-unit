package io.github.ecotrip.adapter;

import java.util.UUID;

import io.github.ecotrip.sensor.Detection;

/**
 * Wrapper of {@link Detection} class.
 */
public class DetectionWrapper {
    private final Detection<UUID> detection;

    private DetectionWrapper(final Detection<UUID> detection) {
        this.detection = detection;
    }

    /**
     * gets the wrapped {@link Detection}
     *
     * @return the wrapped {@link Detection}
     */
    public Detection<UUID> getDetection() {
        return detection;
    }

    /**
     * helper construction method
     *
     * @param detection is the wrapped {@link Detection}
     * @return the instance of {@link DetectionWrapper}
     */
    public static DetectionWrapper of(final Detection<UUID> detection) {
        return new DetectionWrapper(detection);
    }
}
