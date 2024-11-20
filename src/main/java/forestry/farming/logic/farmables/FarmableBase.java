package forestry.farming.logic.farmables;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.crops.CropDestroy;

public abstract class FarmableBase implements IFarmable {
	protected final ItemStack germling;
	protected final BlockState plantedState;
	protected final BlockState matureState;
	protected final boolean replant;

	public FarmableBase(ItemStack germling, BlockState plantedState, BlockState matureState, boolean replant) {
		this.germling = germling;
		this.plantedState = plantedState;
		this.matureState = matureState;
		this.replant = replant;
	}

	@Override
	public boolean isSaplingAt(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() == plantedState.getBlock() && state != matureState;
	}

	@Override
	public ICrop getCropAt(Level level, BlockPos pos, BlockState state) {
		if (state != matureState) {
			return null;
		}

		BlockState replantState = replant ? plantedState : null;
		return new CropDestroy(level, state, pos, replantState);
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
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		return BlockUtil.setBlockWithPlaceSound(level, pos, plantedState);
	}

	@Override
	public boolean isWindfall(ItemStack stack) {
		return false;
	}
}
