package forestry.apiculture.genetics;

import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.IJubilanceFactory;
import net.minecraft.world.level.block.state.BlockState;

public class JubilanceFactory implements IJubilanceFactory {
	@Override
	public IBeeJubilance getDefault() {
		return DefaultBeeJubilance.INSTANCE;
	}

	@Override
	public IBeeJubilance getHermit() {
		return HermitBeeJubilance.INSTANCE;
	}

	@Override
	public IBeeJubilance getRequiresResource(BlockState... acceptedBlockStates) {
		return new RequiresResourceBeeJubilance(acceptedBlockStates);
	}
}
