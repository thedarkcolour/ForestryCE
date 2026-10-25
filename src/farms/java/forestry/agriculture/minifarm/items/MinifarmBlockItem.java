package forestry.agriculture.minifarm.items;

import forestry.core.platform.item.ForestryBlockItem;
import forestry.agriculture.minifarm.blocks.MinifarmBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MinifarmBlockItem extends ForestryBlockItem<MinifarmBlock> {
	public MinifarmBlockItem(MinifarmBlock block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		String name = getBlock().blockType.getSerializedName();
		return Component.translatable("block.forestry.planter." + (getBlock().isManual() ? "manual" : "managed"), Component.translatable("block.forestry." + name));
	}
}
