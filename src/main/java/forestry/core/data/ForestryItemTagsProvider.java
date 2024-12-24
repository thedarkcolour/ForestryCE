package forestry.core.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.minecraftforge.common.Tags;

import forestry.api.ForestryTags;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreItems;
import forestry.core.items.ItemFruit;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.mail.features.MailItems;

import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryItemTagsProvider {
	public static void addTags(MKTagsProvider<Item> tags) {
		// Copy block tags
		tags.copy(ForestryTags.Blocks.CHARCOAL_BLOCK, ForestryTags.Items.CHARCOAL_BLOCK);
		tags.copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS);
		tags.copy(BlockTags.PLANKS, ItemTags.PLANKS);
		tags.copy(BlockTags.LOGS, ItemTags.LOGS);
		tags.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			tags.copy(type.blockTag, type.itemTag);
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

		tags.tag(ItemTags.SAPLINGS).add(ArboricultureItems.SAPLING.get());
		tags.copy(BlockTags.LEAVES, ItemTags.LEAVES);
		tags.copy(Tags.Blocks.ORES, Tags.Items.ORES);
		tags.copy(ForestryTags.Blocks.ORES_TIN, ForestryTags.Items.ORES_TIN);
		tags.copy(ForestryTags.Blocks.ORES_APATITE, ForestryTags.Items.ORES_APATITE);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_RAW_TIN, ForestryTags.Items.STORAGE_BLOCKS_RAW_TIN);

		tags.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_APATITE, ForestryTags.Items.STORAGE_BLOCKS_APATITE);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_TIN, ForestryTags.Items.STORAGE_BLOCKS_TIN);
		tags.copy(ForestryTags.Blocks.STORAGE_BLOCKS_BRONZE, ForestryTags.Items.STORAGE_BLOCKS_BRONZE);

		tags.copy(BlockTags.DIRT, ItemTags.DIRT);

		// Add item-specific tags
		tags.tag(ForestryTags.Items.GEARS).addTags(ForestryTags.Items.GEARS_BRONZE, ForestryTags.Items.GEARS_COPPER, ForestryTags.Items.GEARS_TIN);
		tags.tag(ForestryTags.Items.GEARS_BRONZE).add(CoreItems.GEAR_BRONZE.item());
		tags.tag(ForestryTags.Items.GEARS_TIN).add(CoreItems.GEAR_TIN.item());
		tags.tag(ForestryTags.Items.GEARS_COPPER).add(CoreItems.GEAR_COPPER.item());
		tags.tag(ForestryTags.Items.GEARS_STONE);

		tags.tag(Tags.Items.INGOTS).addTags(ForestryTags.Items.INGOTS_BRONZE, ForestryTags.Items.INGOTS_TIN);
		tags.tag(ForestryTags.Items.INGOTS_BRONZE).add(CoreItems.INGOT_BRONZE.item());
		tags.tag(ForestryTags.Items.INGOTS_TIN).add(CoreItems.INGOT_TIN.item());

		tags.tag(ForestryTags.Items.DUSTS_ASH).add(CoreItems.ASH.item());
		tags.tag(ForestryTags.Items.GEMS_APATITE).add(CoreItems.APATITE.item());
		tags.tag(ForestryTags.Items.RAW_MATERIALS_TIN).add(CoreItems.RAW_TIN.item());

		tags.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

		tags.tag(Tags.Items.RAW_MATERIALS).addTag(ForestryTags.Items.RAW_MATERIALS_TIN);

		tags.tag(ItemTags.SAPLINGS).add(ArboricultureItems.SAPLING.item());
		tags.tag(ForestryTags.Items.BEE_COMBS).add(ApicultureItems.BEE_COMBS.itemArray());
		tags.tag(ForestryTags.Items.VILLAGE_COMBS).add(ApicultureItems.BEE_COMBS.itemArray());
		tags.tag(ForestryTags.Items.PROPOLIS).add(ApicultureItems.PROPOLIS.itemArray());
		tags.tag(ForestryTags.Items.DROP_HONEY).add(ApicultureItems.HONEY_DROP, ApicultureItems.HONEYDEW);

		tags.copy(Tags.Blocks.ORES, Tags.Items.ORES);

		tags.tag(ForestryTags.Items.STAMPS).add(MailItems.STAMPS.itemArray());

		tags.tag(ForestryTags.Items.FORESTRY_FRUITS).add(CoreItems.FRUITS.itemArray());
		tags.tag(ForestryTags.Items.FRUITS).addTag(ForestryTags.Items.FORESTRY_FRUITS);
		tags.tag(ForestryTags.Items.CHERRY).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.CHERRY));
		tags.tag(ForestryTags.Items.WALNUT).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.WALNUT));
		tags.tag(ForestryTags.Items.CHESTNUT).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.CHESTNUT));
		tags.tag(ForestryTags.Items.LEMON).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.LEMON));
		tags.tag(ForestryTags.Items.PLUM).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.PLUM));
		tags.tag(ForestryTags.Items.DATE).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.DATES));
		tags.tag(ForestryTags.Items.PAPAYA).add(CoreItems.FRUITS.item(ItemFruit.EnumFruit.PAPAYA));

		tags.tag(ForestryTags.Items.DUSTS_ASH).add(CoreItems.ASH.item());
		tags.tag(ForestryTags.Items.SAWDUST).add(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP));

		tags.tag(ForestryTags.Items.CRAFTING_TABLES)
				.addOptionalTag(new ResourceLocation("c", "player_workstations/crafting_tables"))
				.addOptionalTag(new ResourceLocation("c", "workbenches"))
				.addOptionalTag(new ResourceLocation("c", "workbench"))
				.add(Items.CRAFTING_TABLE);

		tags.tag(ForestryTags.Items.SCOOPS).add(ApicultureItems.SCOOP.item());

		tags.tag(ForestryTags.Items.BEES).add(ApicultureItems.BEE_DRONE.get(), ApicultureItems.BEE_PRINCESS.get(), ApicultureItems.BEE_QUEEN.get(), ApicultureItems.BEE_LARVAE.get());
	}
}
