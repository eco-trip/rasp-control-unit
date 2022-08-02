package io.github.ecotrip.energy;

import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.services.ConsumptionService;
import io.github.ecotrip.services.CurrentConsumptionService;
import io.github.ecotrip.services.WaterConsumptionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class ConsumptionServicesTests {
    final DetectionFactory<UUID> df1 = DetectionFactory.of(UUID::randomUUID);
    final DetectionFactory<UUID> df2 = DetectionFactory.of(UUID::randomUUID);

    @Test
    public void testWaterConsumption() {
        //MOCKITO
    }
}
