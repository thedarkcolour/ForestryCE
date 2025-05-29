package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SifterBeeEffect extends ThrottledBeeEffect {
	public SifterBeeEffect() {
		super(true, 550, true, true);
	}

	@Override
	IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getLevel();
		Vec3i area = Bee.getParticleArea(genome, housing);

		BlockPos randomPos = VecUtil.getRandomPositionInArea(level.random, area);

		BlockPos posBlock = randomPos.offset(housing.getBlockPos()).offset(VecUtil.center(area));

		if (level.hasChunkAt(posBlock)) {
			BlockState state = level.getBlockState(posBlock);
			Block block = state.getBlock();
			if (block != Blocks.COARSE_DIRT && state.is(BlockTags.DIRT)) {
				level.setBlockAndUpdate(posBlock, Blocks.COARSE_DIRT.defaultBlockState());
			}
		}

		return storedData;
	}
}
