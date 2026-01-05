package forestry.farming.logic.crops;

import forestry.api.farming.ICrop;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Crop implements ICrop {
	private final Level world;
	protected final BlockPos position;

	protected Crop(Level world, BlockPos position) {
		this.world = world;
		this.position = position;
	}

	protected abstract boolean isCrop(Level world, BlockPos pos);

	protected abstract List<ItemStack> harvestBlock(Level world, BlockPos pos);

	@Nullable
	@Override
	public List<ItemStack> harvest() {
		if (!isCrop(this.world, this.position)) {
			return null;
		}

		return harvestBlock(this.world, this.position);
	}

	@Override
	public BlockPos getPosition() {
		return this.position;
	}
}
