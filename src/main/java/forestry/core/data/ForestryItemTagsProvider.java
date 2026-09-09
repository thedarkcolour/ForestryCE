package forestry.core.data;

import forestry.api.ForestryTags;
import forestry.apiculture.bees.EnumPropolis;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.wood.VanillaWoodType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.content.resources.EnumCraftingMaterial;
import forestry.core.platform.item.FruitItemType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryItemTagsProvider {
	public static void addTags(MKTagsProvider<Item> tags) {
		// Copy block tags
		tags.copy(ForestryTags.Blocks.CHARCOAL_BLOCK, ForestryTags.Items.CHARCOAL_BLOCK);
		tags.copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS);
		tags.copy(BlockTags.PLANKS, ItemTags.PLANKS);
		tags.copy(BlockTags.LOGS, ItemTags.LOGS);
		tags.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
		tags.copy(BlockTags.CEILING_HANGING_SIGNS, ItemTags.HANGING_SIGNS);
		tags.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			tags.copy(type.blockTag, type.itemTag);
			tags.copy(type.fireproofBlockTag, type.fireproofItemTag);
		}
		for (VanillaWoodType type : VanillaWoodType.VALUES) {
			tags.copy(type.fireproofBlockTag, type.fireproofItemTag);
		}
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.PLANKS_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.SLABS_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.STAIRS_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.LOGS_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.WOOD_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.STRIPPED_WOOD_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.STRIPPED_LOGS_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.FENCES_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.FENCE_GATES_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.PLANKS_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.SLABS_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.STAIRS_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.LOGS_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.WOOD_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.STRIPPED_WOOD_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.STRIPPED_LOGS_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.FENCES_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ArboricultureBlocks.FENCE_GATES_VANILLA_FIREPROOF.getItems().toArray(Item[]::new));
		tags.copy(BlockTags.STAIRS, ItemTags.STAIRS);
		tags.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		tags.copy(BlockTags.FENCES, ItemTags.FENCES);
		tags.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
		tags.copy(Tags.Blocks.FENCES, Tags.Items.FENCES);
		tags.copy(Tags.Blocks.FENCE_GATES, Tags.Items.FENCE_GATES);
		tags.copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
		tags.copy(BlockTags.SLABS, ItemTags.SLABS);
		tags.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		tags.copy(BlockTags.DOORS, ItemTags.DOORS);
		tags.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
		// The decorative stone and brick walls are the only forestry blocks in BlockTags.WALLS
		tags.copy(BlockTags.WALLS, ItemTags.WALLS);

		// Metal plating and candles. The block tags are named in the singular and the item tags in the
		// plural, so these are added rather than copied
		tags.tag(ForestryTags.Items.METAL_PLATING).add(CoreBlocks.METAL_PLATING.getItems().toArray(Item[]::new));
		tags.tag(ForestryTags.Items.JUMBO_CANDLES).add(CoreBlocks.JUMBO_CANDLES.getItems().toArray(Item[]::new));
		tags.tag(ForestryTags.Items.BIG_CANDLES).add(CoreBlocks.BIG_CANDLES.getItems().toArray(Item[]::new));
		tags.tag(ItemTags.CANDLES).add(CoreBlocks.RAINBOW_CANDLE.item(), CoreBlocks.REFRACTORY_CANDLE.item());

		tags.tag(ItemTags.SAPLINGS).add(ArboricultureItems.TREE_SAPLING.get());
		tags.copy(BlockTags.LEAVES, ItemTags.LEAVES);
		tags.copy(Tags.Blocks.ORES, Tags.Items.ORES);
		tags.copy(ForestryTags.Blocks.ORES_TIN, ForestryTags.Items.ORES_TIN);
		tags.copy(ForestryTags.Blocks.ORES_APATITE, ForestryTags.Items.ORES_APATITE);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_RAW_TIN, ForestryTags.Items.STORAGE_BLOCKS_RAW_TIN);

		tags.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_APATITE, ForestryTags.Items.STORAGE_BLOCKS_APATITE);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_TIN, ForestryTags.Items.STORAGE_BLOCKS_TIN);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_BRONZE, ForestryTags.Items.STORAGE_BLOCKS_BRONZE);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_AMBER, ForestryTags.Items.STORAGE_BLOCKS_AMBER);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_SILICON, ForestryTags.Items.STORAGE_BLOCKS_SILICON);

		tags.copy(BlockTags.DIRT, ItemTags.DIRT);

		// Add item-specific tags
		tags.tag(ForestryTags.Items.GRASSES).add(Items.FERN, Items.LARGE_FERN, Items.SHORT_GRASS, Items.TALL_GRASS);
		tags.tag(ForestryTags.Items.GEARS).addTags(ForestryTags.Items.GEARS_BRONZE, ForestryTags.Items.GEARS_COPPER, ForestryTags.Items.GEARS_TIN, ForestryTags.Items.GEARS_IRON);
		tags.tag(ForestryTags.Items.GEARS_BRONZE).add(CoreItems.GEAR_BRONZE.item());
		tags.tag(ForestryTags.Items.GEARS_TIN).add(CoreItems.GEAR_TIN.item());
		tags.tag(ForestryTags.Items.GEARS_IRON).add(CoreItems.GEAR_IRON.item());
		tags.tag(ForestryTags.Items.GEARS_COPPER).add(CoreItems.GEAR_COPPER.item());
		tags.tag(ForestryTags.Items.GEARS_STONE);

		tags.tag(Tags.Items.INGOTS).addTags(ForestryTags.Items.INGOTS_BRONZE, ForestryTags.Items.INGOTS_TIN);
		tags.tag(ForestryTags.Items.INGOTS_BRONZE).add(CoreItems.BRONZE_INGOT.item());
		tags.tag(ForestryTags.Items.INGOTS_TIN).add(CoreItems.TIN_INGOT.item());
		tags.tag(ForestryTags.Items.NUGGETS_TIN).add(CoreItems.TIN_NUGGET.item());

		tags.tag(ForestryTags.Items.DUSTS_ASH).add(CoreItems.ASH.item());
		tags.tag(ForestryTags.Items.GEMS_APATITE).add(CoreItems.APATITE.item());
		tags.tag(ForestryTags.Items.GEMS_AMBER).add(CoreItems.AMBER.item());
		tags.tag(ForestryTags.Items.SILICON).add(CoreItems.SILICON.item());
		tags.tag(ForestryTags.Items.RAW_MATERIALS_TIN).add(CoreItems.RAW_TIN.item());

		tags.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

		tags.tag(Tags.Items.RAW_MATERIALS).addTag(ForestryTags.Items.RAW_MATERIALS_TIN);

		tags.tag(ItemTags.SAPLINGS).add(ArboricultureItems.TREE_SAPLING.item());
		tags.tag(ForestryTags.Items.BEE_COMBS).add(ApicultureItems.BEE_COMBS.itemArray());
		tags.tag(ForestryTags.Items.VILLAGE_COMBS).add(ApicultureItems.BEE_COMBS.itemArray());
		tags.tag(ForestryTags.Items.PROPOLIS).add(ApicultureItems.PROPOLIS.itemArray());
		tags.tag(ForestryTags.Items.DROP_HONEY).add(CoreItems.HONEY_DROP, CoreItems.HONEYDEW, ApicultureItems.EXPERIENCE_DROP, ApicultureItems.MAGMATIC_DROP);

		tags.copy(Tags.Blocks.ORES, Tags.Items.ORES);

		// The caterpillar entry is added by the butterflies jar, which merges into this tag from its own file
		tags.tag(ForestryTags.Items.GENETIC_SAMPLES).add(ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL).item());

		tags.tag(ForestryTags.Items.FORESTRY_FRUITS).add(CoreItems.FRUITS.itemArray());
		tags.tag(ForestryTags.Items.FRUITS).addTag(ForestryTags.Items.FORESTRY_FRUITS);
		tags.tag(ForestryTags.Items.CHERRY).add(CoreItems.FRUITS.item(FruitItemType.CHERRY));
		tags.tag(ForestryTags.Items.WALNUT).add(CoreItems.FRUITS.item(FruitItemType.WALNUT));
		tags.tag(ForestryTags.Items.CHESTNUT).add(CoreItems.FRUITS.item(FruitItemType.CHESTNUT));
		tags.tag(ForestryTags.Items.LEMON).add(CoreItems.FRUITS.item(FruitItemType.LEMON));
		tags.tag(ForestryTags.Items.PLUM).add(CoreItems.FRUITS.item(FruitItemType.PLUM));
		tags.tag(ForestryTags.Items.DATE).add(CoreItems.FRUITS.item(FruitItemType.DATE));
		tags.tag(ForestryTags.Items.PAPAYA).add(CoreItems.FRUITS.item(FruitItemType.PAPAYA));
		tags.tag(ForestryTags.Items.PEAR).add(CoreItems.FRUITS.item(FruitItemType.PEAR));
		tags.tag(ForestryTags.Items.ORANGE).add(CoreItems.FRUITS.item(FruitItemType.ORANGE));
		tags.tag(ForestryTags.Items.FEIJOA).add(CoreItems.FRUITS.item(FruitItemType.FEIJOA));
		tags.tag(ForestryTags.Items.COCONUT).add(CoreItems.FRUITS.item(FruitItemType.COCONUT));
		tags.tag(ForestryTags.Items.OLIVE).add(CoreItems.FRUITS.item(FruitItemType.OLIVE));

		tags.tag(ForestryTags.Items.DUSTS_ASH).add(CoreItems.ASH.item());
		tags.tag(ForestryTags.Items.SAWDUST).add(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP));

		// legacy aliases from mods that never moved onto the common tag
		tags.tag(Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
			.addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "workbenches"))
			.addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "workbench"))
			.add(Items.CRAFTING_TABLE);

		tags.tag(ForestryTags.Items.SCOOPS).add(CoreItems.SCOOP.item());
		tags.tag(ForestryTags.Items.SCOOPS).add(CoreItems.PROVEN_SCOOP.item());

		tags.tag(ForestryTags.Items.BEES).add(ApicultureItems.BEE_DRONE.get(), ApicultureItems.BEE_PRINCESS.get(), ApicultureItems.BEE_QUEEN.get(), ApicultureItems.BEE_LARVAE.get());
		tags.tag(ItemTags.BOATS).add(ArboricultureItems.BOAT.itemArray());
		tags.tag(ItemTags.CHEST_BOATS).add(ArboricultureItems.CHEST_BOAT.itemArray());

		tags.tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(CoreItems.SURVIVALISTS_PICKAXE);
		tags.tag(ItemTags.PICKAXES).add(CoreItems.SURVIVALISTS_PICKAXE);
		tags.tag(ItemTags.SHOVELS).add(CoreItems.SURVIVALISTS_SHOVEL);

		tags.tag(ForestryTags.Items.BURN_BARREL_BLACKLIST).add(
			Items.LAVA_BUCKET,
			Items.BLAZE_ROD
		);

		tags.tag("curios:head").add(CoreItems.SPECTACLES);
	}
}
