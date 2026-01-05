package forestry.core.items;

import forestry.core.items.definitions.EnumCraftingMaterial;
import net.minecraft.world.item.Item;

public class ItemCraftingMaterial extends ItemForestry {
	private final EnumCraftingMaterial type;

	public ItemCraftingMaterial(EnumCraftingMaterial type) {
		super(new Item.Properties());
		this.type = type;
	}

	public EnumCraftingMaterial getType() {
		return this.type;
	}
}
