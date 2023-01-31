package io.github.ecotrip.adapter;

import java.util.UUID;

import io.github.ecotrip.sensor.Detection;

/**
 * Wrapper of {@link Detection} class.
 */
public class DetectionWrapper {
    private final int sampleDuration;

    private final String stayId;

    private final Detection<UUID> detection;

    private DetectionWrapper(final Detection<UUID> detection, final int sampleDuration, final String stayId) {
        this.detection = detection;
        this.sampleDuration = sampleDuration;
        this.stayId = stayId;
    }

    /**
     * Gets the sample duration
     *
     * @return the sample duration in seconds.
     */
    public int getSampleDuration() {
        return sampleDuration;
    }

    /**
     * Gets the stayId
     *
     * @return the stayId.
     */
    public String getStayId() {
        return stayId;
    }

    /**
     * Gets the wrapped {@link Detection}
     *
     * @return the wrapped {@link Detection}
     */
    public Detection<UUID> getDetection() {
        return detection;
    }

    /**
     * Helper construction method
     *
     * @param stayId id which identifies the client stay
     * @param sampleDurationInSeconds sample rate in seconds
     * @param detection is the wrapped {@link Detection}
     * @return the instance of {@link DetectionWrapper}
     */
    public static DetectionWrapper of(
            final Detection<UUID> detection,
            final int sampleDurationInSeconds,
            final String stayId) {
        return new DetectionWrapper(detection, sampleDurationInSeconds, stayId);
    }
}
