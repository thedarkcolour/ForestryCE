package forestry.core.tiles;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemStackDisplay {
	@OnlyIn(Dist.CLIENT)
	void handleItemStackForDisplay(ItemStack itemStack);
}
