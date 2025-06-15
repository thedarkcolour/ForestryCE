package forestry.farming.items;

import forestry.core.items.ItemBlockForestry;
import forestry.core.utils.ItemTooltipUtil;
import forestry.farming.blocks.FarmBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemBlockFarm extends ItemBlockForestry<FarmBlock> {
	public ItemBlockFarm(FarmBlock block) {
		super(block, new Item.Properties());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag advanced) {
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable("block.forestry.farm.tooltip").withStyle(ChatFormatting.GRAY));
		} else {
			ItemTooltipUtil.addShiftInformation(tooltip);
		}
	}

	@Override
	public String getDescriptionId() {
		FarmBlock block = getBlock();
		return "block.forestry.farm_" + block.getType().identifier();
	}
}
