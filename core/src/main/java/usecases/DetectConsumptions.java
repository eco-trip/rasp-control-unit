package usecases;

import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.services.ConsumptionService;

import java.util.concurrent.CompletableFuture;

public class DetectConsumptions {
    public static <ID> CompletableFuture<Detection<ID>> detectCurrent(ConsumptionService<ID> consumptionService) {
        return consumptionService.getConsumption();
    }

    public static <ID> CompletableFuture<Detection<ID>> detectFlowRate(ConsumptionService<ID> consumptionService) {
        return consumptionService.getConsumption();
    }
}
