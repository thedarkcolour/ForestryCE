package forestry.mail.items;

import forestry.api.mail.EnumPostage;
import forestry.api.mail.IStampItem;
import forestry.core.items.ItemOverlay;
import net.minecraft.world.item.ItemStack;

public class ItemStamp extends ItemOverlay implements IStampItem {
	private final EnumStampDefinition def;

	public ItemStamp(EnumStampDefinition def) {
		super(def);
		this.def = def;
	}

	@Override
	public EnumPostage getPostage(ItemStack stack) {
		return this.def.getPostage();
	}
}
