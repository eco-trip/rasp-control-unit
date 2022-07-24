package io.github.ecotrip.energy;

import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.measures.water.Liter;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.services.ConsumptionService;
import io.github.ecotrip.services.CurrentConsumptionService;
import io.github.ecotrip.services.WaterConsumptionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class ConsumptionServicesTests {
    final DetectionFactory<UUID, FlowRate> df1 = DetectionFactory.of(UUID::randomUUID);
    final DetectionFactory<UUID, Current> df2 = DetectionFactory.of(UUID::randomUUID);
    final ConsumptionService<UUID, Liter, FlowRate> waterConsumptionService = WaterConsumptionService.of(df1);
    final ConsumptionService<UUID, Double, Current> currentConsumptionService = CurrentConsumptionService.of(df2);

    @Test
    public void testWaterConsumption() {
        //MOCKITO
        var detections = List.of(df1.create(FlowRate.of(Liter.of(5))), df2.createEmpty());
        detections.forEach(System.out::println);
    }
}
