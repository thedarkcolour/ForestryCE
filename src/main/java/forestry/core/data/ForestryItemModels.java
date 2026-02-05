package forestry.core.data;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreItems;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.resources.ResourceLocation;
import thedarkcolour.modkit.data.MKItemModelProvider;

import java.util.Set;

public class ForestryItemModels {
	public static void addModels(MKItemModelProvider models) {
		models.generic2d(ApicultureItems.HONEY_DROP);
		models.generic2d(ApicultureItems.HONEYDEW);
		models.generic2d(ApicultureItems.EXPERIENCE_DROP);
		models.generic2d(ApicultureItems.HONEY_POT);
		models.generic2d(ApicultureItems.HONEYED_SLICE);

		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			models.generic2d(ArboricultureItems.BOAT.get(type));
			models.generic2d(ArboricultureItems.CHEST_BOAT.get(type));
		}

		models.generic2d(CoreItems.CARTON.get());
		models.generic2d(CoreItems.BROKEN_BRONZE_PICKAXE.get());
		models.generic2d(CoreItems.BROKEN_BRONZE_SHOVEL.get());
		models.generic2d(CoreItems.BROKEN_BRONZE_AXE.get());
		models.generic2d(CoreItems.BROKEN_BRONZE_SWORD.get());
		models.generic2d(CoreItems.BROKEN_BRONZE_HOE.get());
		models.handheld(CoreItems.BRONZE_PICKAXE.id());
		models.handheld(CoreItems.BRONZE_SHOVEL.id());
		models.handheld(CoreItems.BRONZE_AXE.id());
		models.handheld(CoreItems.BRONZE_SWORD.id());
		models.handheld(CoreItems.BRONZE_HOE.id());
		models.generic2d(CoreItems.KIT_SHOVEL.get());
		models.generic2d(CoreItems.KIT_PICKAXE.get());
		models.generic2d(CoreItems.KIT_AXE.get());
		models.generic2d(CoreItems.KIT_SWORD.get());
		models.generic2d(CoreItems.KIT_HOE.get());

		Set<ResourceLocation> vanillaIds = Set.of(
			ForestryTreeSpecies.OAK,
			ForestryTreeSpecies.DARK_OAK,
			ForestryTreeSpecies.BIRCH,
			ForestryTreeSpecies.ACACIA_VANILLA,
			ForestryTreeSpecies.SPRUCE,
			ForestryTreeSpecies.JUNGLE,
			ForestryTreeSpecies.CHERRY_VANILLA
		);
		// Saplings
		for (ITreeSpecies species : SpeciesUtil.getAllTreeSpecies()) {
			if (vanillaIds.contains(species.id())) continue;

			String name = species.id().getPath().substring("tree_".length()) + "_sapling";
			models.cross("block/" + name, models.modLoc("item/" + name));
			models.generic2d(models.modLoc(name));
		}
	}
}
