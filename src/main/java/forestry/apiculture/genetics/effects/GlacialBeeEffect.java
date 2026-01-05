package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class GlacialBeeEffect extends ThrottledBeeEffect {
	public GlacialBeeEffect() {
		super(false, 200, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getWorldObj();

		if (housing.temperature().isWarmerOrEqual(TemperatureType.WARM)) {
			return storedData;
		}

		Vec3i area = Bee.getParticleArea(genome, housing);
		BlockPos centerPos = housing.getCoordinates().offset(VecUtil.center(area));

		for (int i = 0; i < 10; i++) {

			BlockPos posBlock = VecUtil.getRandomPositionInArea(level.random, area).offset(centerPos);

			// Freeze water
			if (level.hasChunkAt(posBlock)) {
				Block block = level.getBlockState(posBlock).getBlock();
				if (block == Blocks.WATER) {
					if (level.isEmptyBlock(new BlockPos(posBlock.getX(), posBlock.getY() + 1, posBlock.getZ()))) {
						level.setBlockAndUpdate(posBlock, Blocks.ICE.defaultBlockState());
					}
				}
			}
		}

		return storedData;
	}
}
