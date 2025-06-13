package forestry.core.utils;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.config.ForestryConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TreeUtil {
	/**
	 * Gets tree from a location. Does not convert the pollen source to Forestry leaves.
	 */
	@Nullable
	public static ITree getTreeSafe(LevelAccessor level, final BlockPos pos) {
		if (!level.hasChunkAt(pos)) {
			return null;
		}

		if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
			return leaves.getTree();
		}

		BlockState state = level.getBlockState(pos);

		return SpeciesUtil.TREE_TYPE.get().getVanillaIndividual(state);
	}

	public static boolean canCreateNursery(LevelAccessor world, BlockPos pos) {
		return getTreeSafe(world, pos) != null;
	}

	@Nullable
	public static IButterflyNursery getNursery(LevelAccessor level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof IButterflyNursery nursery) {
			return nursery;
		}
		return null;
	}

	@Nullable
	public static IButterflyNursery getOrCreateNursery(LevelAccessor level, BlockPos pos, boolean convertVanilla) {
		IButterflyNursery nursery = getNursery(level, pos);
		if (nursery == null && convertVanilla) {
			return getOrCreateLeaves(level, pos, true);
		}
		return nursery;
	}

	/**
	 * Returns a TileLeaves that can be mated. This will convert vanilla leaves to Forestry leaves.
	 */
	@Nullable
	public static TileLeaves getOrCreateLeaves(LevelAccessor level, final BlockPos pos, boolean convertVanilla) {
		if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
			return leaves;
		} else if (convertVanilla) {
			ITree tree = getTreeSafe(level, pos);

			if (tree != null) {
				ITreeSpecies species = tree.getSpecies();

				species.setLeaves(tree.getGenome(), level, pos, level.getRandom(), true);

				if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
					return leaves;
				}
			}
		}
		return null;
	}

	// assumes serverside
	public static boolean tryMate(TileLeaves leaves, ITree pollen) {
		ITree tree = leaves.getTree();

		if (canMate(tree, pollen)) {
			tree.setMate(pollen.getGenome());
			leaves.sendNetworkUpdate();
			return true;
		}
		return false;
	}

	public static boolean canMate(@Nullable ITree leaves, ITree pollen) {
		return leaves != null && leaves.getMate() == null && (ForestryConfig.SERVER.treesSelfPollination.get() || !leaves.getGenome().isSameAlleles(pollen.getGenome()));
	}
}
