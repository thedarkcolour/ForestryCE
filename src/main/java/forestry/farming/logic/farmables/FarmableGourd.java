package forestry.farming.logic.farmables;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import forestry.farming.logic.crops.CropDestroy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class FarmableGourd implements IFarmable {
	private final ItemStack seed;
	private final Block stem;
	private final Block fruit;

	public FarmableGourd(ItemStack seed, Block stem, Block fruit) {
		this.seed = seed;
		this.stem = stem;
		this.fruit = fruit;
	}

	@Override
	public boolean isSaplingAt(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() == this.stem;
	}

	@Override
	public ICrop getCropAt(Level level, BlockPos pos, BlockState state) {
		if (state.getBlock() != this.fruit) {
			return null;
		}

		return new CropDestroy(level, state, pos, null);
	}

	@Override
	public boolean isGermling(ItemStack stack) {
		return ItemStack.isSameItem(stack, this.seed);
	}

	@Override
	public void addGermlings(Consumer<ItemStack> accumulator) {
		accumulator.accept(this.seed);
	}

	@Override
	public void addProducts(Consumer<ItemStack> accumulator) {
		accumulator.accept(new ItemStack(this.fruit));
	}

	@Override
	public boolean isWindfall(ItemStack stack) {
		return false;
	}

	@Override
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		return BlockUtil.setBlockWithPlaceSound(level, pos, this.stem.defaultBlockState());
	}
}
