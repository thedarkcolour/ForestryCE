package forestry.farming.logic.farmables;

import javax.annotation.Nullable;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.crops.CropChorusFlower;

public enum FarmableChorus implements IFarmable {
	INSTANCE;

	private final ItemStack germling;
	private final ItemStack fruit;

	FarmableChorus() {
		this.germling = new ItemStack(Blocks.CHORUS_FLOWER);
		this.fruit = new ItemStack(Items.CHORUS_FRUIT);
	}

	@Override
	public boolean isSaplingAt(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() == Blocks.CHORUS_FLOWER;
	}

	@Nullable
	@Override
	public ICrop getCropAt(Level level, BlockPos pos, BlockState state) {
		if (state.getBlock() != Blocks.CHORUS_FLOWER) {
			return null;
		}

		if (state.getValue(ChorusFlowerBlock.AGE) < 5) {
			return null;
		}

		return new CropChorusFlower(level, pos);
	}

	@Override
	public boolean isGermling(ItemStack stack) {
		return ItemStack.isSameItem(this.germling, stack);
	}

	@Override
	public void addGermlings(Consumer<ItemStack> accumulator) {
		accumulator.accept(this.germling);
	}

	@Override
	public void addProducts(Consumer<ItemStack> accumulator) {
		accumulator.accept(this.germling);
		accumulator.accept(this.fruit);
	}

	@Override
	public boolean isWindfall(ItemStack stack) {
		return ItemStack.isSameItem(this.fruit, stack);
	}

	@Override
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		if (!canPlace(level, pos)) {
			return false;
		}
		return BlockUtil.setBlockWithPlaceSound(level, pos, Blocks.CHORUS_FLOWER.defaultBlockState());
	}

	private boolean canPlace(Level world, BlockPos position) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos offsetPosition = position.offset(x, 0, z);
				BlockState state = world.getBlockState(offsetPosition);
				if (state.getBlock() == Blocks.CHORUS_FLOWER || state.getBlock() == Blocks.CHORUS_PLANT) {
					return false;
				}
			}
		}

		return true;
	}
}
