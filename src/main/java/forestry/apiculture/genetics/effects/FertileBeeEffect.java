package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FertileBeeEffect extends ThrottledBeeEffect {
	private static final int MAX_BLOCK_FIND_TRIES = 5;

	public FertileBeeEffect() {
		super(false, 6, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getLevel();
		BlockPos housingCoordinates = housing.getBlockPos();
		Vec3i area = Bee.getParticleArea(genome, housing);

		int blockX = getRandomOffset(level.random, housingCoordinates.getX(), area.getX());
		int blockZ = getRandomOffset(level.random, housingCoordinates.getZ(), area.getZ());
		int blockMaxY = housingCoordinates.getY() + area.getY() / 2 + 1;
		int blockMinY = housingCoordinates.getY() - area.getY() / 2 - 1;

		for (int attempt = 0; attempt < MAX_BLOCK_FIND_TRIES; ++attempt) {
			if (level.getChunkSource().getChunkNow(blockX >> 4, blockZ >> 4) != null) {
				if (tryTickColumn(level, blockX, blockZ, blockMaxY, blockMinY)) {
					break;
				}
				blockX = getRandomOffset(level.random, housingCoordinates.getX(), area.getX());
				blockZ = getRandomOffset(level.random, housingCoordinates.getZ(), area.getZ());
			}
		}

		return storedData;
	}

	private static int getRandomOffset(RandomSource random, int centrePos, int offset) {
		return centrePos + random.nextInt(offset) - offset / 2;
	}

	private static boolean tryTickColumn(Level level, int x, int z, int maxY, int minY) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, maxY, z);

		for (int y = maxY; y >= minY; --y) {
			pos.setY(y);

			BlockState state = level.getBlockState(pos);
			Block block = state.getBlock();

			if (state.isRandomlyTicking() && (block instanceof BonemealableBlock)) {
				level.scheduleTick(pos, block, 5);
				return true;
			}
		}
		return false;
	}
}
