package forestry.agriculture.data;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.common.Tags;

import thedarkcolour.modkit.data.MKRecipeProvider;

import forestry.agriculture.features.FarmingBlocks;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.api.ForestryTags;
import forestry.core.content.resources.EnumElectronTube;
import forestry.core.engine.circuits.EnumCircuitBoardType;
import forestry.core.features.CoreItems;

import static thedarkcolour.modkit.data.MKRecipeProvider.path;

/**
 * Generates the crafting recipes for the farms jar. Every farm block and every planter is built from
 * core casings and tubes, so the recipes ship from here rather than from core.
 */
public class AgricultureRecipeProvider {
	public static void addRecipes(RecipeOutput output, MKRecipeProvider recipes) {
		registerCultivationRecipes(recipes);
		registerFarmingRecipes(recipes);
	}

	private static void registerCultivationRecipes(MKRecipeProvider recipes) {
		for (MinifarmBlockType planter : MinifarmBlockType.VALUES) {
			Block managed = FarmingBlocks.MANAGED_MINIFARM.get(planter).block();
			Block manual = FarmingBlocks.MANUAL_MINIFARM.get(planter).block();

			recipes.shapedCrafting(RecipeCategory.MISC, managed, recipe -> {
				recipe.define('G', Tags.Items.GLASS_BLOCKS_COLORLESS);
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

	private static void registerFarmingRecipes(MKRecipeProvider recipes) {
		for (MultifarmMaterialType material : MultifarmMaterialType.values()) {
			Item base = material.getBase().asItem();
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(MultifarmBlockType.PLAIN, material), recipe -> {
				recipe.define('I', Tags.Items.INGOTS_COPPER);
				recipe.define('#', base);
				recipe.define('C', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.TIN));
				recipe.define('W', ItemTags.WOODEN_SLABS);
				recipe.pattern("I#I");
				recipe.pattern("WCW");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(MultifarmBlockType.GEARBOX, material), recipe -> {
				recipe.define('T', ForestryTags.Items.GEARS_TIN);
				recipe.define('#', base);
				recipe.pattern(" # ");
				recipe.pattern("TTT");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(MultifarmBlockType.HATCH, material), recipe -> {
				recipe.define('T', ForestryTags.Items.GEARS_TIN);
				recipe.define('#', base);
				recipe.define('D', ItemTags.WOODEN_TRAPDOORS);
				recipe.pattern(" # ");
				recipe.pattern("TDT");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(MultifarmBlockType.VALVE, material), recipe -> {
				recipe.define('T', ForestryTags.Items.GEARS_TIN);
				recipe.define('#', base);
				recipe.define('X', Tags.Items.GLASS_BLOCKS_COLORLESS);
				recipe.pattern(" # ");
				recipe.pattern("XTX");
			});
			recipes.shapedCrafting(RecipeCategory.MISC, FarmingBlocks.FARM.get(MultifarmBlockType.CONTROL, material), recipe -> {
				recipe.define('T', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD));
				recipe.define('#', base);
				recipe.define('X', Tags.Items.DUSTS_REDSTONE);
				recipe.pattern(" # ");
				recipe.pattern("XTX");
			});
		}
	}

	// The tube a planter is built around says what it farms
	private static EnumElectronTube getElectronTube(MinifarmBlockType planter) {
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
}
