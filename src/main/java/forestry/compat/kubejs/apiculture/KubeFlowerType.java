package forestry.compat.kubejs.apiculture;

import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.apiculture.IFlowerType;

public record KubeFlowerType(BiPredicate<Level, BlockPos> isAcceptableFlower, PlantRandomFlowerFunction plantRandomFlower, boolean dominant) implements IFlowerType {
	@Override
	public boolean isAcceptableFlower(Level level, BlockPos pos) {
		return this.isAcceptableFlower.test(level, pos);
	}

	@Override
	public boolean plantRandomFlower(Level level, BlockPos pos, List<BlockState> nearbyFlowers) {
		return this.plantRandomFlower.plantRandomFlower(level, pos, nearbyFlowers);
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	public interface PlantRandomFlowerFunction {
		boolean plantRandomFlower(Level level, BlockPos pos, List<BlockState> nearbyFlowers);
	}
}
