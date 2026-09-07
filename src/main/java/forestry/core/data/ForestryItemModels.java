package forestry.core.data;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreItems;
import forestry.core.content.resources.EnumCraftingMaterial;
import forestry.core.content.resources.EnumElectronTube;
import forestry.core.engine.circuits.EnumCircuitBoardType;
import forestry.core.platform.item.FruitItemType;
import forestry.core.platform.util.SpeciesUtil;
import forestry.core.platform.registration.FeatureItem;
import forestry.core.content.backpacks.features.CrateItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import thedarkcolour.modkit.data.MKItemModelProvider;

import java.util.Set;

public class ForestryItemModels {
	public static void addModels(MKItemModelProvider models) {
		models.generic2d(CoreItems.AMBER);
		models.generic2d(ApicultureItems.AMBER_DRONE);
		models.generic2d(ArboricultureItems.AMBER_SAPLING_FOSSIL);
		models.generic2d(ApicultureItems.AMBROSIA);
		models.generic2d(CoreItems.APATITE);
		models.generic2d(ApicultureItems.APIARIST_HELMET);
		models.generic2d(ApicultureItems.APIARIST_LEGS);
		models.generic2d(ApicultureItems.APIARIST_CHEST);
		models.generic2d(ApicultureItems.APIARIST_BOOTS);
		models.generic2d(CoreItems.ASH);
		models.generic2d(CoreItems.ASH_BRICK);
		models.generic2d(ApicultureItems.SMOKER);
		models.generic2d(CoreItems.BEESWAX);
		models.generic2d(CoreItems.BITUMINOUS_PEAT);
		models.generic2d(CoreItems.COMPOST);
		models.generic2d(CrateItems.CRATE);
		models.generic2d(CoreItems.DECAYING_WHEAT);
		models.generic2d(CoreItems.DISSIPATION_CHARGE);
		models.generic2d(CoreItems.FERTILIZER_COMPOUND);
		models.generic2d(CoreItems.FLEXIBLE_CASING);
		models.generic2d(CoreItems.FORESTERS_MANUAL);
		models.generic2d(ArboricultureItems.GRAFTER);
		models.generic2d(CoreItems.HARDENED_CASING);
		models.generic2d(CoreItems.IMPREGNATED_CASING);
		models.generic2d(ApicultureItems.FRAME_IMPREGNATED);
		models.generic2d(CoreItems.IODINE_CHARGE);
		models.generic2d(CoreItems.MOULDY_WHEAT);
		models.generic2d(CoreItems.MULCH);
		models.generic2d(CoreItems.PEAT);
		models.generic2d(CoreItems.PORTABLE_ALYZER);
		models.generic2d(ApicultureItems.FRAME_PROVEN);
		models.generic2d(ArboricultureItems.PROVEN_GRAFTER);
		models.generic2d(CoreItems.PROVEN_SCOOP);
		models.generic2d(CoreItems.REFRACTORY_WAX);
		models.generic2d(CoreItems.REFRACTORY_WAX_BRICK);
		models.generic2d(CoreItems.WAX_BRICK);
		models.generic2d(ApicultureItems.ROYAL_JELLY);
		models.generic2d(CoreItems.SCOOP);
		models.generic2d(CoreItems.SILICON);
		models.generic2d(CoreItems.SOLAR_CELL);
		models.generic2d(CoreItems.SOLDERING_IRON);
		models.generic2d(CoreItems.SPECTACLES);
		models.generic2d(CoreItems.STURDY_CASING);
		models.generic2d(CoreItems.TIN_NUGGET);
		models.generic2d(ApicultureItems.FRAME_UNTREATED);

		// what kind of fruit is this?
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.CHERRY));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.CHESTNUT));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.COCONUT));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.DATE));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.FEIJOA));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.LEMON));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.OLIVE));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.ORANGE));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.PAPAYA));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.PEAR));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.PLUM));
		models.generic2d(CoreItems.FRUITS.get(FruitItemType.WALNUT));

		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PULSATING_MESH));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOOD_PULP));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHOR));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHORESCENT_JELLY));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.IMPREGNATED_STICK));
		models.generic2d(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SCENTED_PANELING));

		// Used by FilledCrateModel.Loader#FILLED_CRATE_LOCATION
		models.generic2d(models.modLoc("filled_crate"));

		models.handheld(models.modLoc("wrench"));

		for (FeatureItem<?> comb : ApicultureItems.BEE_COMBS.getFeatures()) {
			models.withExistingParent(comb.id().getPath(), models.modLoc("item/bee_combs"));
		}

		// Electron tubes each have their own texture (1.20.1's per-material set), not a shared
		// tinted layer pair, so they get a single-layer model instead of the Tier 3 loop below.
		for (EnumElectronTube type : EnumElectronTube.values()) {
			layered(models, CoreItems.ELECTRON_TUBES.get(type), "item/electron_tube_" + type.getSerializedName());
		}

		// Circuit boards each have their own texture (1.20.1's per-tier set), not a shared tinted
		// layer pair, so they get a single-layer model like the electron tubes above.
		for (EnumCircuitBoardType type : EnumCircuitBoardType.values()) {
			layered(models, CoreItems.CIRCUITBOARDS.get(type), "item/circuit_board_" + type.getSerializedName());
		}

		// Tier 3: layered `item/generated` models. Every item in a family shares the same texture pair,
		// so pointing at the registered features keeps the model ids from drifting off the item ids.
		for (FeatureItem<?> pollen : ApicultureItems.POLLEN_CLUSTER.getFeatures()) {
			layered(models, pollen, "item/pollen.0", "item/pollen.1");
		}
		layered(models, ArboricultureItems.TREE_POLLEN, "item/pollen.0", "item/pollen.1");

		models.generic2d(CoreItems.HONEY_DROP);
		models.generic2d(CoreItems.HONEYDEW);
		models.generic2d(ApicultureItems.EXPERIENCE_DROP);
		models.generic2d(ApicultureItems.MAGMATIC_DROP);
		models.generic2d(ApicultureItems.HONEYED_SLICE);

		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			models.generic2d(ArboricultureItems.BOAT.get(type));
			models.generic2d(ArboricultureItems.CHEST_BOAT.get(type));
		}

		models.generic2d(CoreItems.CARTON.get());
		models.generic2d(CoreItems.BROKEN_SURVIVALISTS_PICKAXE.get());
		models.generic2d(CoreItems.BROKEN_SURVIVALISTS_SHOVEL.get());
		models.generic2d(CoreItems.BROKEN_SURVIVALISTS_AXE.get());
		models.generic2d(CoreItems.BROKEN_SURVIVALISTS_SWORD.get());
		models.generic2d(CoreItems.BROKEN_SURVIVALISTS_HOE.get());
		models.handheld(CoreItems.SURVIVALISTS_PICKAXE.id());
		models.handheld(CoreItems.SURVIVALISTS_SHOVEL.id());
		models.handheld(CoreItems.SURVIVALISTS_AXE.id());
		models.handheld(CoreItems.SURVIVALISTS_SWORD.id());
		models.handheld(CoreItems.SURVIVALISTS_HOE.id());
		models.generic2d(CoreItems.SHOVEL_KIT.get());
		models.generic2d(CoreItems.PICKAXE_KIT.get());
		models.generic2d(CoreItems.AXE_KIT.get());
		models.generic2d(CoreItems.SWORD_KIT.get());
		models.generic2d(CoreItems.HOE_KIT.get());

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

			// Sapling assets are named "<id>_sapling" (e.g. ipe_sapling).
			String name = species.id().getPath() + "_sapling";
			models.cross("block/" + name, models.modLoc("item/" + name));
			models.generic2d(models.modLoc(name));
		}
	}

	/**
	 * Builds a layered `item/generated` model referencing the given textures in order.
	 */
	public static void layered(MKItemModelProvider models, FeatureItem<?> feature, String... textures) {
		var builder = models.getBuilder(feature.id().getPath()).parent(new ModelFile.UncheckedModelFile("item/generated"));
		for (int i = 0; i < textures.length; i++) {
			builder.texture("layer" + i, models.modLoc(textures[i]));
		}
	}
}
