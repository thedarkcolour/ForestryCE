package forestry.api.arboriculture;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface ICharcoalPileWall {
	int getCharcoalAmount();

	boolean matches(BlockState state);

	List<ItemStack> getDisplayItems();
}
