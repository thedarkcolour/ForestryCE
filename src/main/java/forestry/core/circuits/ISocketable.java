package forestry.core.circuits;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ISocketable {
	int getSocketCount();

	ItemStack getSocket(int slot);

	void setSocket(int slot, ItemStack stack);

	ResourceLocation getSocketType();
}
