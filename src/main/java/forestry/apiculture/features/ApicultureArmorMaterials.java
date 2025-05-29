package forestry.apiculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

@FeatureProvider
public class ApicultureArmorMaterials {
	public static final DeferredRegister<ArmorMaterial> REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE).getRegistry(Registries.ARMOR_MATERIAL);

	public static final Holder<ArmorMaterial> APIARIST = REGISTRY.register("apiarist", () -> new ArmorMaterial(
		Map.of(
			ArmorItem.Type.HELMET, 1,
			ArmorItem.Type.CHESTPLATE, 3,
			ArmorItem.Type.LEGGINGS, 2,
			ArmorItem.Type.BOOTS, 1
			// todo doggy armor
		),
		15,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		() -> Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK).get()),
		IDK,
		0.0f,
		0.0f
	));
}
