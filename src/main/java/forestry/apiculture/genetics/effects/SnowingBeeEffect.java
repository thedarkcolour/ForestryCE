package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.render.ParticleRender;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SnowingBeeEffect extends ThrottledBeeEffect {
	public SnowingBeeEffect() {
		super(false, 20, true, true);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();

		if (housing.temperature().isWarmerOrEqual(TemperatureType.WARM)) {
			return storedData;
		}

		Vec3i area = Bee.getParticleArea(genome, housing);

		BlockPos randomPos = VecUtil.getRandomPositionInArea(level.random, area);
		BlockPos posBlock = randomPos.offset(housing.getCoordinates()).offset(VecUtil.center(area));

		// Put snow on the ground
		if (level.hasChunkAt(posBlock)) {
			BlockState state = level.getBlockState(posBlock);
			Block block = state.getBlock();
			if (!state.isAir() && block != Blocks.SNOW || !Blocks.SNOW.defaultBlockState().canSurvive(level, posBlock)) {
				return storedData;
			}

			if (block == Blocks.SNOW) {
				int layers = state.getValue(SnowLayerBlock.LAYERS);
				if (layers < 7) {
					BlockState moreSnow = state.setValue(SnowLayerBlock.LAYERS, layers + 1);
					level.setBlockAndUpdate(posBlock, moreSnow);
				} else {
					level.setBlockAndUpdate(posBlock, Blocks.SNOW.defaultBlockState());
				}
			} else if (block.defaultBlockState().canBeReplaced()) {
				level.setBlockAndUpdate(posBlock, Blocks.SNOW.defaultBlockState());
			}
		}

		return storedData;
	}

	@Override
	public IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();

		if (level.random.nextInt(3) == 0) {
			Vec3i area = Bee.getParticleArea(genome, housing);

			BlockPos coordinates = housing.getCoordinates();

			BlockPos spawn = VecUtil.getRandomPositionInArea(level.random, area).offset(coordinates).offset(VecUtil.center(area));
			ParticleRender.addEntitySnowFX(level, spawn.getX(), spawn.getY(), spawn.getZ());
			return storedData;
		} else {
			return super.doFX(genome, storedData, housing);
		}
	}
}
