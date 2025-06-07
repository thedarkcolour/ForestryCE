package forestry.core.data;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.data.models.FilledCrateModelBuilder;
import forestry.core.fluids.ForestryFluids;
import forestry.core.utils.ModUtil;
import forestry.cultivation.features.CultivationBlocks;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.modules.features.ModFeatureRegistry;
import forestry.storage.features.CrateItems;
import forestry.storage.items.ItemBackpack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import thedarkcolour.modkit.data.MKItemModelProvider;

import java.util.stream.Stream;

import static forestry.api.ForestryConstants.forestry;
import static forestry.core.data.models.ForestryBlockStateProvider.file;

public class ForestryItemModels {
	public static void addModels(MKItemModelProvider models) {
		// Resources
		models.generic2d(ApicultureItems.HONEY_DROP);
		models.generic2d(ApicultureItems.HONEYDEW);
		models.generic2d(ApicultureItems.EXPERIENCE_DROP);
		models.generic2d(ApicultureItems.HONEY_POT);
		models.generic2d(ApicultureItems.HONEYED_SLICE);

		// Boats
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			models.generic2d(ArboricultureItems.BOAT.get(type));
			models.generic2d(ArboricultureItems.CHEST_BOAT.get(type));
		}

		// Butterfly items
		models.withExistingParent(LepidopterologyItems.CATERPILLAR.getName(), models.mcLoc("item/generated"))
			.texture("layer0", forestry("item/caterpillar.body2"))
			.texture("layer1", forestry("item/caterpillar.body"));
		models.withExistingParent(LepidopterologyItems.SERUM.getName(), models.mcLoc("item/generated"))
			.texture("layer0", forestry("item/liquids/jar.bottle"))
			.texture("layer1", forestry("item/liquids/jar.contents"));

		// Crates
		CrateItems.getCrates().forEach(featureCrated -> {
			Item containedItem = featureCrated.get().getContained().getItem();
			String id = featureCrated.getName();

			if (ApicultureItems.BEE_COMBS.itemEqual(containedItem)) {
				filledCrateModelLayered(models, id, forestry("item/bee_combs.0"), forestry("item/bee_combs.1"));
			} else if (ApicultureItems.POLLEN_CLUSTER.itemEqual(containedItem)) {
				filledCrateModelLayered(models, id, forestry("item/pollen.0"), forestry("item/pollen.1"));
			} else {
				ResourceLocation contained = ModUtil.getRegistryName(containedItem);
				ResourceLocation contentsTexture;

				if (containedItem instanceof BlockItem && !(containedItem instanceof ItemNameBlockItem)) {
					contentsTexture = contained.withPrefix("block/");
				} else {
					contentsTexture = contained.withPrefix("item/");
				}

				filledCrateModel(models, id, contentsTexture);
			}
		});

		// 2d items
		ApicultureItems.FRAME_CREATIVE.getItems().forEach(models::generic2d);

		// manual overrides
		filledCrateModel(models, CrateItems.CRATED_CACTUS.getName(), models.mcLoc("block/cactus_side"));
		filledCrateModel(models, CrateItems.CRATED_MYCELIUM.getName(), models.mcLoc("block/mycelium_side"));
		filledCrateModel(models, CrateItems.CRATED_GRASS_BLOCK.getName(), models.mcLoc("block/grass_block_top"));
		filledCrateModel(models, CrateItems.CRATED_PROPOLIS.getName(), forestry("item/propolis.0"));

		// Farm blocks
		Stream.of(CultivationBlocks.MANAGED_PLANTER, CultivationBlocks.MANUAL_PLANTER)
			.flatMap(g -> g.getFeatureByType().entrySet().stream())
			.forEachOrdered(entry -> models.withExistingParent(entry.getValue().getName(), forestry("block/" + entry.getKey().getSerializedName())));

		// Buckets
		for (ForestryFluids fluid : ForestryFluids.values()) {
			models.getBuilder(path(fluid.getBucket()))
				.customLoader(DynamicFluidContainerModelBuilder::begin)
				.fluid(fluid.getFluid())
				.end()
				.parent(models.getExistingFile(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "item/bucket")));
		}

		// Backpacks
		for (Holder<Item> object : ModFeatureRegistry.get(ForestryModuleIds.STORAGE).getRegistry(Registries.ITEM).getEntries()) {
			if (object.value() instanceof ItemBackpack) {
				String path = object.getKey().location().getPath();
				boolean woven = path.endsWith("woven");

				models.withExistingParent(path, woven ? forestry("item/backpack/woven_neutral") : forestry("item/backpack/normal_neutral"))
					.override(file(woven ? forestry("item/backpack/woven_locked") : forestry("item/backpack/normal_locked"))).predicate(models.mcLoc("mode"), 1).end()
					.override(file(woven ? forestry("item/backpack/woven_receive") : forestry("item/backpack/normal_receive"))).predicate(models.mcLoc("mode"), 2).end()
					.override(file(woven ? forestry("item/backpack/woven_resupply") : forestry("item/backpack/normal_resupply"))).predicate(models.mcLoc("mode"), 3).end();
			}
		}
	}

	private static String path(Item item) {
		return ModUtil.getRegistryName(item).getPath();
	}

	private static void filledCrateModel(MKItemModelProvider models, String id, ResourceLocation texture) {
		models.getBuilder(id)
			.customLoader(FilledCrateModelBuilder::begin)
			.layer1(texture)
			.end();
	}

	private static void filledCrateModelLayered(MKItemModelProvider models, String id, ResourceLocation layer1, ResourceLocation layer2) {
		models.getBuilder(id)
			.customLoader(FilledCrateModelBuilder::begin)
			.layer1(layer1)
			.layer2(layer2)
			.end();
	}
}
