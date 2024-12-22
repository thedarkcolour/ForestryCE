package forestry.arboriculture.genetics;

import javax.annotation.Nullable;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.apiculture.genetics.IBee;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.pollen.ForestryPollenTypes;
import forestry.api.genetics.pollen.IPollen;
import forestry.api.genetics.pollen.IPollenType;
import forestry.arboriculture.blocks.BlockDefaultLeaves;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.config.ForestryConfig;
import forestry.core.utils.SpeciesUtil;
import forestry.core.utils.TreeUtil;

public class TreePollenType implements IPollenType<ITree> {
	@Override
	public ResourceLocation id() {
		return ForestryPollenTypes.TREE;
	}

	@Override
	public boolean canPollinate(LevelAccessor level, BlockPos pos, @Nullable Object pollinator) {
		if (level.getBlockEntity(pos) instanceof TileLeaves) {
			return true;
		} else {
			BlockState state = level.getBlockState(pos);

			// Don't pollinate persistent leaves
			Optional<Boolean> persistent = state.getOptionalValue(BlockDefaultLeaves.PERSISTENT);
			if (persistent.isPresent() && persistent.get()) {
				return false;
			}

			// Don't pollinate decorative leaves
			ITree individual = SpeciesUtil.TREE_TYPE.get().getVanillaIndividual(state);
			if (individual != null) {
				return !individual.getSpecies().getDecorativeLeaves().is(state.getBlock().asItem());
			}
		}

		return false;
	}

	@Nullable
	@Override
	public IPollen<ITree> tryCollectPollen(LevelAccessor level, BlockPos pos, @Nullable Object pollinator) {
		if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
			ITree tree = leaves.getTree();
			if (tree != null) {
				return new TreePollen(tree);
			}
		}

		ITree vanillaIndividual = SpeciesUtil.TREE_TYPE.get().getVanillaIndividual(level.getBlockState(pos));
		return vanillaIndividual == null ? null : new TreePollen(vanillaIndividual);
	}

	@Override
	public boolean tryPollinate(LevelAccessor level, BlockPos pos, ITree pollen, @Nullable Object pollinator) {
		boolean convertVanilla = pollinator instanceof IBee && ForestryConfig.SERVER.pollinateVanillaLeaves.get();
		TileLeaves leaves = TreeUtil.getOrCreateLeaves(level, pos, convertVanilla);

		// check if tree can accept a mate (pollen)
		return leaves != null && TreeUtil.tryMate(leaves, pollen);
	}

	@Override
	public IPollen<ITree> readNbt(Tag nbt) {
		return new TreePollen(SpeciesUtil.deserializeIndividual(SpeciesUtil.TREE_TYPE.get(), nbt));
	}

	public class TreePollen implements IPollen<ITree> {
		private final ITree pollen;

		public TreePollen(ITree pollen) {
			this.pollen = pollen;
		}

		@Override
		public IPollenType<ITree> getType() {
			return TreePollenType.this;
		}

		@Override
		public ITree getPollen() {
			return this.pollen;
		}

		@Nullable
		@Override
		public Tag writeNbt() {
			return SpeciesUtil.serializeIndividual(this.pollen);
		}

		@Override
		public ItemStack createStack() {
			return this.pollen.createStack(TreeLifeStage.POLLEN);
		}
	}
}
