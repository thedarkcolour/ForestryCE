package forestry.agriculture.minifarm.tiles;

import forestry.api.agriculture.ForestryFarmTypes;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.agriculture.features.FarmingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PeatBogBlockEntity extends AbstractMinifarmBlockEntity {
	public PeatBogBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.BOG.tileType(), pos, state, ForestryFarmTypes.PEAT);
	}

	@Override
	public List<ItemStack> createGermlingStacks() {
		return List.of();
	}

	@Override
	public List<ItemStack> createResourceStacks() {
		return List.of(
			CoreBlocks.BOG_EARTH.stack(),
			CoreBlocks.BOG_EARTH.stack(),
			CoreBlocks.BOG_EARTH.stack(),
			CoreBlocks.BOG_EARTH.stack()
		);
	}

	@Override
	public List<ItemStack> createProductionStacks() {
		return List.of(
			CoreItems.PEAT.stack(),
			CoreItems.PEAT.stack(),
			CoreItems.PEAT.stack(),
			CoreItems.PEAT.stack()
		);
	}
}
