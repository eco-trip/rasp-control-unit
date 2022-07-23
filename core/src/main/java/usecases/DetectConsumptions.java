package usecases;

import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.Detection;
import io.github.ecotrip.services.ConsumptionService;

public class DetectConsumptions {
    public static <ID> Detection<ID, Current> detectCurrent(ConsumptionService<ID, Current> consumptionService) {
        return consumptionService.getConsumption();
    }

    public static <ID> Detection<ID, FlowRate> detectFlowRate(ConsumptionService<ID, FlowRate> consumptionService) {
        return consumptionService.getConsumption();
    }
}
