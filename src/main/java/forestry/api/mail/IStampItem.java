package forestry.api.mail;

import net.minecraft.world.item.ItemStack;

public interface IStampItem {
	EnumPostage getPostage(ItemStack stack);
}
