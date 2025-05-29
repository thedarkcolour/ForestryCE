package forestry.arboriculture.blocks;

import com.mojang.authlib.GameProfile;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.client.IForestryClientApi;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.core.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Genetic leaves with no tile entity, used for worldgen trees.
 * Similar to decorative leaves, but these will drop saplings and can be used for pollination.
 */
public class BlockDefaultLeavesFruit extends BlockAbstractLeaves {
	private final ForestryLeafType type;

	public BlockDefaultLeavesFruit(ForestryLeafType type) {
		this.type = type;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		ITree tree = getTree(level, pos);
		if (tree == null) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		BlockUtil.sendDestroyEffects(level, pos, state);
		IFruit fruitProvider = tree.getGenome().getActiveValue(TreeChromosomes.FRUIT);
		List<ItemStack> products = tree.produceStacks(level, pos, fruitProvider.getRipeningPeriod());
		level.setBlock(pos, ArboricultureBlocks.LEAVES_DEFAULT.get(this.type).defaultState()
			.setValue(LeavesBlock.PERSISTENT, state.getValue(LeavesBlock.PERSISTENT))
			.setValue(LeavesBlock.DISTANCE, state.getValue(LeavesBlock.DISTANCE)), Block.UPDATE_CLIENTS);
		for (ItemStack fruit : products) {
			ItemHandlerHelper.giveItemToPlayer(player, fruit);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	protected void getLeafDrop(List<ItemStack> drops, Level level, BlockPos pos, @Nullable GameProfile profile, float saplingModifier, int fortune, LootParams.Builder context) {
		ITree tree = this.type.getIndividual();

		// Add saplings
		List<ITree> saplings = tree.getSaplings(level, pos, profile, saplingModifier);
		for (ITree sapling : saplings) {
			if (sapling != null) {
				drops.add(sapling.createStack(TreeLifeStage.SAPLING));
			}
		}

		// Add fruits
		IGenome genome = tree.getGenome();
		IFruit fruitProvider = genome.getActiveValue(TreeChromosomes.FRUIT);
		if (fruitProvider.isFruitLeaf()) {
			List<ItemStack> produceStacks = tree.produceStacks(level, pos, Integer.MAX_VALUE);
			drops.addAll(produceStacks);
		}
	}

	public ResourceLocation getSpeciesId() {
		return this.type.getSpeciesId();
	}

	public ForestryLeafType getType() {
		return this.type;
	}

	@Override
	protected ITree getTree(BlockGetter world, BlockPos pos) {
		return this.type.getIndividual();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int colorMultiplier(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		if (tintIndex == BlockAbstractLeaves.FRUIT_COLOR_INDEX) {
			IFruit genome = this.type.getFruit();
			return genome.getDecorativeColor();
		}

		return IForestryClientApi.INSTANCE.getTreeManager().getTint(this.type.getIndividual().getSpecies()).get(level, pos);
	}
}
