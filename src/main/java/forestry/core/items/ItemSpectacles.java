package forestry.core.items;

import forestry.api.ForestryConstants;
import forestry.api.core.ISpectacleVision;
import forestry.core.config.Constants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemSpectacles extends ArmorItem {
	public static final String TEXTURE_NATURALIST_ARMOR_PRIMARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/naturalist_armor_1.png";
	public static final ISpectacleVision VISION = (player, armor, execute) -> true;

	public ItemSpectacles() {
		super(ArmorMaterials.LEATHER, Type.HELMET, (new Item.Properties()).durability(100));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return TEXTURE_NATURALIST_ARMOR_PRIMARY;
	}
}
