package forestry.agriculture.minifarm.tiles;

import forestry.api.agriculture.ForestryFarmTypes;
import forestry.agriculture.features.FarmingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ArboretumBlockEntity extends AbstractMinifarmBlockEntity {
	public ArboretumBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.ARBORETUM.tileType(), pos, state, ForestryFarmTypes.ARBOREAL);
	}

	@Override
	public List<ItemStack> createGermlingStacks() {
		return List.of(
			new ItemStack(Blocks.OAK_SAPLING),
			new ItemStack(Blocks.BIRCH_SAPLING),
			new ItemStack(Blocks.BIRCH_SAPLING),
			new ItemStack(Blocks.OAK_SAPLING)
		);
	}

	@Override
	public List<ItemStack> createResourceStacks() {
		return List.of(
			new ItemStack(Blocks.DIRT),
			new ItemStack(Blocks.DIRT),
			new ItemStack(Blocks.DIRT),
			new ItemStack(Blocks.DIRT)
		);
	}

	@Override
	public List<ItemStack> createProductionStacks() {
		return List.of(
			new ItemStack(Blocks.OAK_LOG),
			new ItemStack(Items.APPLE),
			new ItemStack(Items.APPLE),
			new ItemStack(Blocks.OAK_LOG)
		);
	}
}
