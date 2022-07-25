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
    final ConsumptionService<UUID> waterConsumptionService = WaterConsumptionService.of(df1);
    final ConsumptionService<UUID> currentConsumptionService = CurrentConsumptionService.of(df2);

    @Test
    public void testWaterConsumption() {
        //MOCKITO
        var detections = List.of(df1.create(FlowRate.of(5)), df2.createEmpty());
        detections.forEach(System.out::println);
    }
}
