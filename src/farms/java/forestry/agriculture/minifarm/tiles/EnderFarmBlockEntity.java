package forestry.agriculture.minifarm.tiles;

import forestry.api.agriculture.ForestryFarmTypes;
import forestry.agriculture.features.FarmingBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EnderFarmBlockEntity extends AbstractMinifarmBlockEntity {
	public EnderFarmBlockEntity(BlockPos pos, BlockState state) {
		super(FarmingBlockEntities.ENDER.tileType(), pos, state, ForestryFarmTypes.ENDER);
	}

	@Override
	public List<ItemStack> createGermlingStacks() {
		return List.of(
			new ItemStack(Blocks.CHORUS_FLOWER),
			new ItemStack(Blocks.CHORUS_FLOWER),
			new ItemStack(Blocks.CHORUS_FLOWER),
			new ItemStack(Blocks.CHORUS_FLOWER)
		);
	}

	@Override
	public List<ItemStack> createResourceStacks() {
		return List.of(
			new ItemStack(Blocks.END_STONE),
			new ItemStack(Blocks.END_STONE),
			new ItemStack(Blocks.END_STONE),
			new ItemStack(Blocks.END_STONE)
		);
	}

	@Override
	public List<ItemStack> createProductionStacks() {
		return List.of(
			new ItemStack(Blocks.CHORUS_FLOWER),
			new ItemStack(Items.CHORUS_FRUIT),
			new ItemStack(Items.CHORUS_FRUIT),
			new ItemStack(Blocks.CHORUS_FLOWER)
		);
	}
}
