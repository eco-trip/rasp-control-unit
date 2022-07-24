package usecases;

import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.measures.water.Liter;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.services.ConsumptionService;

import java.util.concurrent.CompletableFuture;

public class DetectConsumptions {
    public static <ID> CompletableFuture<Detection<ID, Current>> detectCurrent(
            ConsumptionService<ID, Double, Current> consumptionService) {
        return consumptionService.getConsumption();
    }

    public static <ID> CompletableFuture<Detection<ID, FlowRate>> detectFlowRate(
            ConsumptionService<ID, Liter, FlowRate> consumptionService) {
        return consumptionService.getConsumption();
    }
}
