package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.features.CultivationBlocks;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.features.FarmingBlocks;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import thedarkcolour.modkit.data.MKRecipeProvider;

import static thedarkcolour.modkit.data.MKRecipeProvider.path;

class CultivationRecipes {
	static void registerCultivationRecipes(MKRecipeProvider recipes) {
		for (BlockTypePlanter planter : BlockTypePlanter.values()) {
			Block managed = CultivationBlocks.MANAGED_PLANTER.get(planter).block();
			Block manual = CultivationBlocks.MANUAL_PLANTER.get(planter).block();

			recipes.shapedCrafting(RecipeCategory.MISC, managed, recipe -> {
				recipe.define('G', Tags.Items.GLASS_BLOCKS);
				recipe.define('T', CoreItems.ELECTRON_TUBES.get(getElectronTube(planter)));
				recipe.define('C', CoreItems.FLEXIBLE_CASING);
				recipe.define('B', CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.BASIC));
				recipe.pattern("GTG");
				recipe.pattern("TCT");
				recipe.pattern("GBG");
			});
			recipes.shapelessCrafting(RecipeCategory.MISC, manual, 1, managed);
			recipes.shapelessCrafting(path(managed) + "_from_manual", RecipeCategory.MISC, managed, 1, manual);
		}
	}

	static EnumElectronTube getElectronTube(BlockTypePlanter planter) {
		return switch (planter) {
			case ARBORETUM -> EnumElectronTube.GOLD;
			case FARM_CROPS -> EnumElectronTube.BRONZE;
			case PEAT_POG -> EnumElectronTube.OBSIDIAN;
			case FARM_MUSHROOM -> EnumElectronTube.APATITE;
			case FARM_GOURD -> EnumElectronTube.LAPIS;
			case FARM_NETHER -> EnumElectronTube.BLAZE;
			case FARM_ENDER -> EnumElectronTube.ENDER;
		};
	}

	static void registerFarmingRecipes(MKRecipeProvider recipes) {
		for (EnumFarmMaterial material : EnumFarmMaterial.values()) {
			Item base = material.getBase().asItem();
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(EnumFarmBlockType.PLAIN, material), recipe -> {
				recipe.define('I', Items.COPPER_INGOT);
				recipe.define('#', base);
				recipe.define('C', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.TIN));
				recipe.define('W', ItemTags.WOODEN_SLABS);
				recipe.pattern("I#I");
				recipe.pattern("WCW");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(EnumFarmBlockType.GEARBOX, material), recipe -> {
				recipe.define('T', ForestryTags.Items.GEARS_TIN);
				recipe.define('#', base);
				recipe.pattern(" # ");
				recipe.pattern("TTT");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(EnumFarmBlockType.HATCH, material), recipe -> {
				recipe.define('T', ForestryTags.Items.GEARS_TIN);
				recipe.define('#', base);
				recipe.define('D', ItemTags.WOODEN_TRAPDOORS);
				recipe.pattern(" # ");
				recipe.pattern("TDT");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(EnumFarmBlockType.VALVE, material), recipe -> {
				recipe.define('T', ForestryTags.Items.GEARS_TIN);
				recipe.define('#', base);
				recipe.define('X', Tags.Items.GLASS_BLOCKS);
				recipe.pattern(" # ");
				recipe.pattern("XTX");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(EnumFarmBlockType.CONTROL, material), recipe -> {
				recipe.define('T', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD));
				recipe.define('#', base);
				recipe.define('X', Tags.Items.DUSTS_REDSTONE);
				recipe.pattern(" # ");
				recipe.pattern("XTX");
			});
		}
	}
}
