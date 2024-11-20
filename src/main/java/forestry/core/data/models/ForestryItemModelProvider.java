package forestry.core.data.models;

import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.data.builder.FilledCrateModelBuilder;
import forestry.core.fluids.ForestryFluids;
import forestry.core.utils.ModUtil;
import forestry.cultivation.blocks.BlockPlanter;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.features.CultivationBlocks;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.ModFeatureRegistry;
import forestry.storage.features.CrateItems;
import forestry.storage.items.ItemBackpack;
import forestry.storage.items.ItemCrated;

import static forestry.core.data.models.ForestryBlockStateProvider.file;

public class ForestryItemModelProvider extends ItemModelProvider {
	public ForestryItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, ForestryConstants.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		withExistingParent(LepidopterologyItems.CATERPILLAR_GE.getName(), mcLoc("item/generated"))
				.texture("layer0", ForestryConstants.forestry("item/caterpillar.body2"))
				.texture("layer1", ForestryConstants.forestry("item/caterpillar.body"));
		withExistingParent(LepidopterologyItems.SERUM_GE.getName(), mcLoc("item/generated"))
				.texture("layer0", ForestryConstants.forestry("item/liquids/jar.bottle"))
				.texture("layer1", ForestryConstants.forestry("item/liquids/jar.contents"));

		for (FeatureItem<ItemCrated> featureCrated : CrateItems.getCrates()) {
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
					contentsTexture = new ResourceLocation(contained.getNamespace(), "block/" + contained.getPath());
				} else {
					contentsTexture = new ResourceLocation(contained.getNamespace(), "item/" + contained.getPath());
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
		filledCrateModel(CrateItems.CRATED_PROPOLIS.getName(), modLoc("item/propolis.0"));

		for (Map.Entry<BlockTypePlanter, FeatureBlock<BlockPlanter, BlockItem>> cell : CultivationBlocks.MANAGED_PLANTER.getFeatureByType().entrySet()) {
			Block block = cell.getValue().block();
			withExistingParent(ForestryBlockStateProvider.path(block), ForestryConstants.forestry("block/" + cell.getKey().getSerializedName()));
		}

		// Buckets
		for (ForestryFluids fluid : ForestryFluids.values()) {
			BucketItem item = fluid.getBucket();
			if (item != null) {
				getBuilder(path(item))
						.customLoader(DynamicFluidContainerModelBuilder::begin)
						.fluid(fluid.getFluid())
						.end()
						.parent(getExistingFile(new ResourceLocation("forge:item/bucket")));
			}
		}

		// Backpacks
		for (RegistryObject<Item> object : ModFeatureRegistry.get(ForestryModuleIds.STORAGE).getRegistry(Registries.ITEM).getEntries()) {
			if (object.get() instanceof ItemBackpack) {
				String path = object.getId().getPath();
				boolean woven = path.endsWith("woven");

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
