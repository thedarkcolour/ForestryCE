package forestry.apiculture.items;

import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemHoneyComb extends ItemForestry implements IColoredItem {
	private final EnumHoneyComb type;

	public ItemHoneyComb(EnumHoneyComb type) {
		super(new Item.Properties());

		this.type = type;
	}

	@Override
	public int getColorFromItemStack(ItemStack itemstack, int tintIndex) {
		EnumHoneyComb honeyComb = this.type;
		if (tintIndex == 1) {
			return honeyComb.primaryColor;
		} else {
			return honeyComb.secondaryColor;
		}
	}
}
