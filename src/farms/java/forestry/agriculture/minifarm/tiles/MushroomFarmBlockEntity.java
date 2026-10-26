package forestry.agriculture.minifarm.tiles;

import forestry.api.agriculture.ForestryFarmTypes;
import forestry.agriculture.features.FarmingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MushroomFarmBlockEntity extends AbstractMinifarmBlockEntity {
	public MushroomFarmBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.MUSHROOM.tileType(), pos, state, ForestryFarmTypes.SHROOM);
	}

	@Override
	public List<ItemStack> createGermlingStacks() {
		return List.of(
			new ItemStack(Blocks.RED_MUSHROOM),
			new ItemStack(Blocks.BROWN_MUSHROOM),
			new ItemStack(Blocks.BROWN_MUSHROOM),
			new ItemStack(Blocks.RED_MUSHROOM)
		);
	}

	@Override
	public List<ItemStack> createResourceStacks() {
		return List.of(
			new ItemStack(Blocks.MYCELIUM),
			new ItemStack(Blocks.PODZOL),
			new ItemStack(Blocks.PODZOL),
			new ItemStack(Blocks.MYCELIUM)
		);
	}

	@Override
	public List<ItemStack> createProductionStacks() {
		return List.of(
			new ItemStack(Blocks.RED_MUSHROOM),
			new ItemStack(Blocks.BROWN_MUSHROOM),
			new ItemStack(Blocks.BROWN_MUSHROOM),
			new ItemStack(Blocks.RED_MUSHROOM)
		);
	}
}
