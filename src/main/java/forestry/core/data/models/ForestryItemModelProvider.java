package forestry.core.data.models;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.features.ApicultureCrates;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.data.builder.FilledCrateModelBuilder;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.util.ModUtil;
import forestry.core.platform.registration.FeatureItem;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.backpacks.features.CrateItems;
import forestry.core.content.backpacks.items.BackpackItem;
import forestry.core.content.backpacks.items.CratedItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import static forestry.core.data.models.ForestryBlockStateProvider.file;

public class ForestryItemModelProvider extends ItemModelProvider {
	public ForestryItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, ForestryConstants.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (FeatureItem<CratedItem> featureCrated : CrateItems.getCrates()) {
			Item containedItem = featureCrated.get().getContained().getItem();
			String id = featureCrated.getName();

			if (ApicultureItems.BEE_COMBS.itemEqual(containedItem)) {
				filledCrateModelLayered(id, modLoc("item/bee_combs.0"), modLoc("item/bee_combs.1"));
			} else if (ApicultureItems.POLLEN_CLUSTER.itemEqual(containedItem)) {
				filledCrateModelLayered(id, modLoc("item/pollen.0"), modLoc("item/pollen.1"));
			} else {
				ResourceLocation contained = ModUtil.getRegistryName(containedItem);
				ResourceLocation contentsTexture;

				if (containedItem instanceof BlockItem && !(containedItem instanceof ItemNameBlockItem)) {
					contentsTexture = ResourceLocation.fromNamespaceAndPath(contained.getNamespace(), "block/" + contained.getPath());
				} else {
					contentsTexture = ResourceLocation.fromNamespaceAndPath(contained.getNamespace(), "item/" + contained.getPath());
				}

				filledCrateModel(id, contentsTexture);

			}
		}

		// 2d items
		basicItem(ApicultureItems.FRAME_CREATIVE.get());

		// manual overrides
		filledCrateModel(CrateItems.CRATED_CACTUS.getName(), mcLoc("block/cactus_side"));
		filledCrateModel(CrateItems.CRATED_MYCELIUM.getName(), mcLoc("block/mycelium_side"));
		filledCrateModel(CrateItems.CRATED_GRASS_BLOCK.getName(), mcLoc("block/grass_block_top"));
		filledCrateModel(ApicultureCrates.CRATED_PROPOLIS.getName(), modLoc("item/propolis.0"));

		// Buckets
		for (ForestryFluids fluid : ForestryFluids.values()) {
			BucketItem item = fluid.getBucket();
			if (item != null) {
				getBuilder(path(item))
					.customLoader(DynamicFluidContainerModelBuilder::begin)
					.fluid(fluid.getFluid())
					.end()
					.parent(getExistingFile(ResourceLocation.parse("neoforge:item/bucket")));
			}
		}

		// Backpacks
		for (DeferredHolder<Item, ? extends Item> object : ModFeatureRegistry.get(ForestryModuleIds.STORAGE).getRegistry(Registries.ITEM).getEntries()) {
			if (object.get() instanceof BackpackItem) {
				String path = object.getId().getPath();
				// Deviation from 1.20.1: the ids here are woven_<type>_backpack, not <type>_bag_woven, so
				// the old endsWith test never matched and every woven bag took the plain model
				boolean woven = path.startsWith("woven");

				withExistingParent(path, woven ? modLoc("item/backpack/woven_neutral") : modLoc("item/backpack/normal_neutral"))
					.override().predicate(mcLoc("mode"), 1).model(file(woven ? modLoc("item/backpack/woven_locked") : modLoc("item/backpack/normal_locked"))).end()
					.override().predicate(mcLoc("mode"), 2).model(file(woven ? modLoc("item/backpack/woven_receive") : modLoc("item/backpack/normal_receive"))).end()
					.override().predicate(mcLoc("mode"), 3).model(file(woven ? modLoc("item/backpack/woven_resupply") : modLoc("item/backpack/normal_resupply"))).end();
			}
		}
	}

	private static String path(Item block) {
		return ModUtil.getRegistryName(block).getPath();
	}

	private void filledCrateModel(String id, ResourceLocation texture) {
		getBuilder(id)
			.customLoader(FilledCrateModelBuilder::begin)
			.layer1(texture)
			.end();
	}

	private void filledCrateModelLayered(String id, ResourceLocation layer1, ResourceLocation layer2) {
		getBuilder(id)
			.customLoader(FilledCrateModelBuilder::begin)
			.layer1(layer1)
			.layer2(layer2)
			.end();
	}
}
