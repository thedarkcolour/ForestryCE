package forestry.core.items;

import forestry.core.features.CoreDataComponents;
import forestry.core.gui.PortableAnalyzerMenu;
import forestry.core.inventory.PortableAnalyzerInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemPortableAnalyzer extends ItemWithGui {
	public ItemPortableAnalyzer() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, ctx, tooltip, advanced);
		Integer charges = stack.get(CoreDataComponents.ANALYZER_CHARGES);
		if (charges == null) {
			charges = 0;
		}
		tooltip.add(Component.translatable(stack.getDescriptionId() + ".charges", charges).withStyle(ChatFormatting.GOLD));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, int slotIndex) {
		return new PortableAnalyzerMenu(windowId, new PortableAnalyzerInventory(playerInv.player, playerInv.getItem(slotIndex)), playerInv);
	}
}
