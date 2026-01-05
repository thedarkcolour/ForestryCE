package forestry.mail.items;

import forestry.api.core.ItemGroups;
import forestry.api.mail.EnumPostage;
import forestry.api.mail.IStamps;
import forestry.core.items.ItemOverlay;
import net.minecraft.world.item.ItemStack;

public class ItemStamp extends ItemOverlay implements IStamps {
	private final EnumStampDefinition def;

	public ItemStamp(EnumStampDefinition def) {
		super(ItemGroups.tabForestry, def);
		this.def = def;
	}

	@Override
	public EnumPostage getPostage(ItemStack itemstack) {
		return this.def.getPostage();
	}
}
