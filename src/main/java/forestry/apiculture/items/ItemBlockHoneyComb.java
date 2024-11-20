package forestry.apiculture.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import forestry.apiculture.blocks.BlockHoneyComb;
import forestry.core.items.ItemBlockForestry;
import forestry.core.items.definitions.IColoredItem;

public class ItemBlockHoneyComb extends ItemBlockForestry<BlockHoneyComb> implements IColoredItem {
	public ItemBlockHoneyComb(BlockHoneyComb block) {
		super(block, new Item.Properties());
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		EnumHoneyComb honeyComb = getBlock().getType();
		if (tintIndex == 1) {
			return honeyComb.primaryColor;
		} else {
			return honeyComb.secondaryColor;
		}
	}
}
