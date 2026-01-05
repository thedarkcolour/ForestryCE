package forestry.farming.items;

import forestry.core.TranslationKeys;
import forestry.core.items.ItemBlockForestry;
import forestry.farming.blocks.FarmBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockFarm extends ItemBlockForestry<FarmBlock> {
	public ItemBlockFarm(FarmBlock block) {
		super(block, new Item.Properties());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable("block.forestry.farm.tooltip").withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.add(Component.translatable(TranslationKeys.HOLD_SHIFT_FOR_DETAILS).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public String getDescriptionId() {
		FarmBlock block = getBlock();
		return "block.forestry.farm_" + block.getType().getSerializedName();
	}
}
