package forestry.apiculture.bees;

import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.item.ITintedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HoneyCombBlockItem extends ForestryBlockItem<BlockHoneyComb> implements ITintedItem {
	@Deprecated
	public HoneyCombBlockItem(BlockHoneyComb block) {
		this(block, new Item.Properties());
	}

	public HoneyCombBlockItem(BlockHoneyComb block, Item.Properties properties) {
		super(block, properties);
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
