package forestry.apiculture;

import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IGenome;
import net.minecraft.core.Vec3i;

public class AlvearyBeeModifier implements IBeeModifier {
	@Override
	public Vec3i modifyTerritory(IGenome genome, Vec3i currentModifier) {
		return currentModifier.multiply(2);
	}
}
