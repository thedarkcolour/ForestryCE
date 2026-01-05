package forestry.arboriculture.charcoal;

import com.google.common.base.Preconditions;
import forestry.api.arboriculture.ICharcoalPileWall;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CharcoalPileWall implements ICharcoalPileWall {

	@Nullable
	private final BlockState blockState;
	@Nullable
	private final Block block;
	private final int charcoalAmount;

	public CharcoalPileWall(BlockState blockState, int charcoalAmount) {
		this.blockState = blockState;
		this.block = null;
		this.charcoalAmount = charcoalAmount;
	}

	public CharcoalPileWall(Block block, int charcoalAmount) {
		this.blockState = null;
		this.block = block;
		this.charcoalAmount = charcoalAmount;
	}

	@Override
	public int getCharcoalAmount() {
		return this.charcoalAmount;
	}

	@Override
	public boolean matches(BlockState state) {
		return this.block == state.getBlock() || this.blockState == state;
	}

	@Override
	public NonNullList<ItemStack> getDisplayItems() {
		if (this.block == null) {
			Preconditions.checkNotNull(this.blockState);
			return NonNullList.withSize(1, new ItemStack(this.blockState.getBlock()));    //TODO loss of properties?
		} else if (this.blockState == null) {
			Preconditions.checkNotNull(this.block);
			return NonNullList.withSize(1, new ItemStack(this.block));
		}
		return NonNullList.create();
	}

}
