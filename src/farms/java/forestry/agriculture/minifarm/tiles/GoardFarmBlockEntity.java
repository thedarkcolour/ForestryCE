package forestry.agriculture.minifarm.tiles;

import forestry.api.agriculture.ForestryFarmTypes;
import forestry.agriculture.features.FarmingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GoardFarmBlockEntity extends AbstractMinifarmBlockEntity {
	public GoardFarmBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.GOURD.tileType(), pos, state, ForestryFarmTypes.GOURD);
	}

	@Override
	public List<ItemStack> createGermlingStacks() {
		return List.of();
	}

	@Override
	public List<ItemStack> createResourceStacks() {
		return List.of();
	}

	@Override
	public List<ItemStack> createProductionStacks() {
		return List.of(
			new ItemStack(Blocks.MELON),
			new ItemStack(Blocks.PUMPKIN),
			new ItemStack(Blocks.PUMPKIN),
			new ItemStack(Blocks.MELON)
		);
	}
}
