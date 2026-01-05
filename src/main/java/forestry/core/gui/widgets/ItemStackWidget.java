package forestry.core.gui.widgets;

import net.minecraft.world.item.ItemStack;

public class ItemStackWidget extends ItemStackWidgetBase {
	private final ItemStack itemStack;

	public ItemStackWidget(WidgetManager widgetManager, int xPos, int yPos, ItemStack itemStack) {
		super(widgetManager, xPos, yPos);
		this.itemStack = itemStack;
	}

	@Override
	public ItemStack getItemStack() {
		return this.itemStack;
	}
}
