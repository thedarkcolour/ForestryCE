package forestry.apiculture;

import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IGenome;

public class ApiaryBeeModifier implements IBeeModifier {
	@Override
	public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
		return 0.1f * currentSpeed;
	}
}
