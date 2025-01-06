package forestry.arboriculture.genetics;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.genetics.ITree;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.tiles.TileSapling;
import forestry.core.tiles.TileUtil;

import forestry.api.genetics.IGenome;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TreeGrowthHelper {
	/**
	 * Finds a valid growth position for a tree if it has all necessary saplings and at least one position has enough
	 * room for the tree to grow. If no valid position is found, then {@code null} is returned.
	 *
	 *
	 * @param level  The world to check for valid positions.
	 * @param genome The genome of the sapling trying to grow.
	 * @param pos The position of the sapling.
	 * @param expectedGirth The expected girth of the tree according to genetics and variation.
	 * @param expectedHeight The expected height of the tree according to genetics and variation.
	 * @return A valid growth position, or {@code null} if saplings were missing or if there's no room for the tree.
	 */
	@Nullable
	public static BlockPos getGrowthPos(LevelAccessor level, IGenome genome, BlockPos pos, int expectedGirth, int expectedHeight) {
		// TODO use MutableBlockPos to reduce BlockPos allocations.
		// Check if the tree has enough saplings to grow
		BlockPos growthPos = hasSufficientSaplingsAroundSapling(genome, level, pos, expectedGirth);
		if (growthPos == null) {
			return null;
		}

		// Check if the trunk would be obstructed by solid blocks
		if (!hasRoom(level, growthPos, expectedGirth, expectedHeight)) {
			return null;
		}

		return growthPos;
	}

	private static boolean hasRoom(LevelAccessor level, BlockPos pos, int expectedGirth, int expectedHeight) {
		Vec3i area = new Vec3i(expectedGirth, expectedHeight + 1, expectedGirth);
		return checkArea(level, pos.above(), area);
	}

	private static boolean checkArea(LevelAccessor level, BlockPos start, Vec3i area) {
		for (int x = 0; x < area.getX(); x++) {
			for (int y = 0; y < area.getY(); y++) {
				for (int z = 0; z < area.getZ(); z++) {
					BlockPos pos = start.offset(x, y, z);
					BlockState blockState = level.getBlockState(pos);
					if (!blockState.canBeReplaced() && !blockState.is(BlockTags.LEAVES)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks an area for saplings.
	 * If saplings need to be 2x2, 3x3, etc, it will check all configurations in which pos is included in that area.
	 * Uses a knownSaplings cache to avoid checking the same saplings multiple times.
	 */
	@Nullable
	private static BlockPos hasSufficientSaplingsAroundSapling(IGenome genome, LevelAccessor world, BlockPos saplingPos, int expectedGirth) {
		final int checkSize = (expectedGirth * 2) - 1;
		final int offset = expectedGirth - 1;
		final Object2BooleanOpenHashMap<BlockPos> knownSaplings = new Object2BooleanOpenHashMap<>(checkSize * checkSize);

		for (int x = -offset; x <= 0; x++) {
			for (int z = -offset; z <= 0; z++) {
				BlockPos startPos = saplingPos.offset(x, 0, z);
				if (checkForSaplings(genome, world, startPos, expectedGirth, knownSaplings)) {
					return startPos;
				}
			}
		}

		return null;
	}

	private static boolean checkForSaplings(IGenome genome, LevelAccessor level, BlockPos startPos, int girth, Object2BooleanOpenHashMap<BlockPos> knownSaplings) {
		for (int x = 0; x < girth; x++) {
			for (int z = 0; z < girth; z++) {
				BlockPos checkPos = startPos.offset(x, 0, z);
				boolean knownSapling = knownSaplings.computeIfAbsent(checkPos, k -> isSapling(genome, level, checkPos));
				if (!knownSapling) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isSapling(IGenome genome, LevelAccessor level, BlockPos pos) {
		if (!level.hasChunkAt(pos)) {
			return false;
		}

		if (level.isEmptyBlock(pos)) {
			return false;
		}

		TileSapling sapling = TileUtil.getTile(level, pos, TileSapling.class);
		if (sapling == null) {
			return false;
		}

		ITree tree = sapling.getTree();
		return tree != null && tree.getGenome().getActiveAllele(TreeChromosomes.SPECIES) == genome.getActiveAllele(TreeChromosomes.SPECIES);
	}
}
