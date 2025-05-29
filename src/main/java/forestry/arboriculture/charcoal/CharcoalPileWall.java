package forestry.arboriculture.charcoal;

import forestry.api.arboriculture.ICharcoalPileWall;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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
	public List<ItemStack> getDisplayItems() {
		if (this.block == null) {
			return Collections.singletonList(new ItemStack(this.blockState.getBlock()));
		} else if (this.blockState == null) {
			return Collections.singletonList(new ItemStack(this.block));
		}
		return List.of();
	}
}
