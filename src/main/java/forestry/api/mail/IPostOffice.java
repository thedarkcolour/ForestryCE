package forestry.api.mail;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public interface IPostOffice {
	void collectPostage(NonNullList<ItemStack> stamps);

	IPostalState lodgeLetter(ServerLevel world, ItemStack itemstack, boolean doLodge);

	ItemStack getAnyStamp(int max);

	ItemStack getAnyStamp(EnumPostage postage, int max);

	ItemStack getAnyStamp(EnumPostage[] postages, int max);
}
