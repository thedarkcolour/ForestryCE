package forestry.farming.logic.farmables;

import com.google.common.collect.ImmutableSet;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.core.utils.SpeciesUtil;
import forestry.farming.logic.crops.CropDestroy;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class FarmableGE implements IFarmable {
	private final ImmutableSet<Item> windfall;

	public FarmableGE() {
		ImmutableSet.Builder<Item> builder = new ImmutableSet.Builder<>();
		for (IFruit fruit : TreeChromosomes.FRUIT.values()) {
			for (IProduct product : fruit.getProducts()) {
				builder.add(product.item());
			}
		}
		this.windfall = builder.build();
	}

	@Override
	public boolean isSaplingAt(Level level, BlockPos pos, BlockState state) {
		return ArboricultureBlocks.SAPLING_GE.blockEqual(state);
	}

	@Override
	@Nullable
	public ICrop getCropAt(Level level, BlockPos pos, BlockState state) {
		if (!state.is(BlockTags.LOGS)) {
			return null;
		}

		return new CropDestroy(level, state, pos, null);
	}

	@Override
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		ITreeSpeciesType treeRoot = SpeciesUtil.TREE_TYPE.get();

		return IIndividualHandlerItem.filter(germling, individual -> individual instanceof ITree tree && treeRoot.plantSapling(level, tree, player.getGameProfile(), pos));
	}

	@Override
	public boolean isGermling(ItemStack stack) {
		return SpeciesUtil.TREE_TYPE.get().isMember(stack);
	}

	@Override
	public boolean isWindfall(ItemStack stack) {
		return this.windfall.contains(stack.getItem());
	}
}
