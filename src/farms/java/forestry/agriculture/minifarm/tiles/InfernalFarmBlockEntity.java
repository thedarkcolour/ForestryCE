package forestry.agriculture.minifarm.tiles;

import forestry.api.agriculture.ForestryFarmTypes;
import forestry.agriculture.features.FarmingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class InfernalFarmBlockEntity extends AbstractMinifarmBlockEntity {
	public InfernalFarmBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.NETHER.tileType(), pos, state, ForestryFarmTypes.INFERNAL);
	}

	@Override
	public List<ItemStack> createGermlingStacks() {
		return List.of(
			new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.NETHER_WART)
		);
	}

	@Override
	public List<ItemStack> createResourceStacks() {
		return List.of(
			new ItemStack(Blocks.SOUL_SAND),
			new ItemStack(Blocks.SOUL_SAND),
			new ItemStack(Blocks.SOUL_SAND),
			new ItemStack(Blocks.SOUL_SAND)
		);
	}

	@Override
	public List<ItemStack> createProductionStacks() {
		return List.of(
			new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.NETHER_WART),
			new ItemStack(Items.NETHER_WART)
		);
	}
}
