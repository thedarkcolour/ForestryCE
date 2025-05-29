package forestry.arboriculture.items;

import forestry.arboriculture.blocks.BlockForestryDoor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ItemBlockWoodDoor extends ItemBlockWood<BlockForestryDoor> {
	public ItemBlockWoodDoor(BlockForestryDoor block) {
		super(block);
	}

	// Copy of DoubleHighBlockItem.placeBlock
	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		Level level = context.getLevel();
		BlockPos blockpos = context.getClickedPos().above();
		BlockState blockstate = level.isWaterAt(blockpos) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
		level.setBlock(blockpos, blockstate, 27);
		return super.placeBlock(context, state);
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		if (getBlock().isFireproof()) {
			return 0;
		} else {
			return 200;
		}
	}
}
