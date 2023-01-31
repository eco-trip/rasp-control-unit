package io.github.ecotrip.adapter;

import java.util.UUID;

import io.github.ecotrip.sensor.Detection;

/**
 * Wrapper of {@link Detection} class.
 */
public class DetectionWrapper {
    private final int sampleDuration;
    private final Detection<UUID> detection;

    private DetectionWrapper(final Detection<UUID> detection, final int sampleDuration) {
        this.detection = detection;
        this.sampleDuration = sampleDuration;
    }

    /**
     * gets the sample duration
     *
     * @return the sample duration in seconds.
     */
    public int getSampleDuration() {
        return sampleDuration;
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
    public static DetectionWrapper of(final Detection<UUID> detection, final int sampleDurationInSeconds) {
        return new DetectionWrapper(detection, sampleDurationInSeconds);
    }
}
