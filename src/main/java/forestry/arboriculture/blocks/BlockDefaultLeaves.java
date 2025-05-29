package forestry.arboriculture.blocks;

import com.mojang.authlib.GameProfile;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.client.IForestryClientApi;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Genetic leaves with no tile entity, used for worldgen trees.
 * Similar to decorative leaves, but these will drop saplings and can be used for pollination.
 */
public class BlockDefaultLeaves extends BlockAbstractLeaves {
	private final ForestryLeafType type;

	public BlockDefaultLeaves(ForestryLeafType type) {
		this.type = type;
	}

	public ResourceLocation getSpeciesId() {
		return this.type.getSpeciesId();
	}

	public ForestryLeafType getType() {
		return this.type;
	}

	@Override
	protected void getLeafDrop(List<ItemStack> drops, Level level, BlockPos pos, @Nullable GameProfile profile, float saplingModifier, int fortune, LootParams.Builder context) {
		ITree tree = this.type.getIndividual();

		// Add saplings
		List<ITree> saplings = tree.getSaplings(level, pos, profile, saplingModifier);
		for (ITree sapling : saplings) {
			if (sapling != null) {
				drops.add(SpeciesUtil.TREE_TYPE.get().createStack(sapling, TreeLifeStage.SAPLING));
			}
		}
	}

	@Override
	protected ITree getTree(BlockGetter world, BlockPos pos) {
		return this.type.getIndividual();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		return IForestryClientApi.INSTANCE.getTreeManager().getTint(this.type.getIndividual().getSpecies()).get(level, pos);
	}
}
