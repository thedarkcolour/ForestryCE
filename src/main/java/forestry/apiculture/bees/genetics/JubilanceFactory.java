package forestry.apiculture.bees.genetics;

import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.IJubilanceFactory;
import forestry.apiculture.bees.genetics.effects.TransformBlockBeeEffect.BlockMatcher;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

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
		return new RequiresResourceBeeJubilance(new BlockMatcher.Direct(Arrays.stream(acceptedBlockStates).map(BlockState::getBlock).distinct().toList()));
	}
}
