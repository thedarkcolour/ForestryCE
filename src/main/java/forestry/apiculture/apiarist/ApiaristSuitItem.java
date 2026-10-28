package forestry.apiculture.apiarist;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.IBeeProtection;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.core.features.CoreItems;
import forestry.core.content.resources.EnumCraftingMaterial;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ApiaristSuitItem extends ArmorItem {
	private static final Holder<ArmorMaterial> APIARIST_ARMOR_MATERIAL = Holder.direct(new ArmorMaterial(
		Map.of(
			ArmorItem.Type.BOOTS, 1,
			ArmorItem.Type.LEGGINGS, 2,
			ArmorItem.Type.CHESTPLATE, 3,
			ArmorItem.Type.HELMET, 1,
			ArmorItem.Type.BODY, 3
		),
		15,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		() -> Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK).get()),
		List.of(new ArmorMaterial.Layer(ForestryConstants.forestry("apiarist_armor"))),
		0.0F,
		0.0F
	));

	public enum BeeProtection implements IBeeProtection {
		INSTANCE;

		@Override
		public boolean protectEntity(LivingEntity entity, ItemStack armor, @Nullable IBeeEffect cause, boolean doProtect) {
			return true;
		}
	}

	public ApiaristSuitItem(ArmorItem.Type type) {
		super(APIARIST_ARMOR_MATERIAL, type, new Item.Properties());
	}
}
