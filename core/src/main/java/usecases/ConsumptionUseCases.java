package usecases;

import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.services.ConsumptionService;
import io.github.ecotrip.services.CurrentConsumptionService;
import io.github.ecotrip.services.WaterConsumptionService;

import java.util.concurrent.CompletableFuture;

public class ConsumptionUseCases<ID> {

    private final CurrentConsumptionService<ID> currentConsumptionService;
    private final WaterConsumptionService<ID> waterConsumptionService;

    private ConsumptionUseCases(final WaterConsumptionService<ID> waterConsumptionService,
                               final CurrentConsumptionService<ID> currentConsumptionService) {
        this.waterConsumptionService = waterConsumptionService;
        this.currentConsumptionService = currentConsumptionService;
    }

    public CompletableFuture<Detection<ID>> detectCurrent() {
        return currentConsumptionService.getConsumption();
    }

    public CompletableFuture<Detection<ID>> detectFlowRate() {
        return waterConsumptionService.getConsumption();
    }

    public static <ID> ConsumptionUseCases<ID> of(final WaterConsumptionService<ID> waterConsumptionService,
                                                  final CurrentConsumptionService<ID> currentConsumptionService) {
        return new ConsumptionUseCases<>(waterConsumptionService, currentConsumptionService);
    }
}
