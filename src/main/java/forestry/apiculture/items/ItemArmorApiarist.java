package forestry.apiculture.items;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.IBeeProtection;
import forestry.api.apiculture.bee.IBeeEffect;
import forestry.apiculture.features.ApicultureArmorMaterials;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.config.Constants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemArmorApiarist extends ArmorItem {
	public static final String TEXTURE_APIARIST_ARMOR_PRIMARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/apiarist_armor_1.png";
	public static final String TEXTURE_APIARIST_ARMOR_SECONDARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/apiarist_armor_2.png";

	public enum BeeProtection implements IBeeProtection {
		INSTANCE;

		@Override
		public boolean doBeeProtection(LivingEntity entity, ItemStack armor, @Nullable IBeeEffect cause, boolean execute) {
			return true;
		}
	}

	public ItemArmorApiarist(ArmorItem.Type type) {
		super(ApicultureArmorMaterials.APIARIST, type, new Item.Properties().durability(type.getDurability(3)));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		if (ApicultureItems.APIARIST_LEGS.itemEqual(stack)) {
			return TEXTURE_APIARIST_ARMOR_SECONDARY;
		} else {
			return TEXTURE_APIARIST_ARMOR_PRIMARY;
		}
	}
}
