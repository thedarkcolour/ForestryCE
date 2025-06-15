package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreDataComponents;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.features.CultivationBlocks;
import forestry.factory.recipes.MoistenerRecipe;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.features.FarmingBlocks;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
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

		// Manure and Fertilizer
		recipes.shapedCrafting("compost_wheat", RecipeCategory.MISC, CoreItems.COMPOST, 4, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', Tags.Items.CROPS_WHEAT);
			recipe.pattern(" X ");
			recipe.pattern("X#X");
			recipe.pattern(" X ");
		});

		recipes.shapedCrafting("compost_ash", RecipeCategory.MISC, CoreItems.COMPOST, 1, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', ForestryTags.Items.DUSTS_ASH);
			recipe.pattern(" X ");
			recipe.pattern("X#X");
			recipe.pattern(" X ");
		});

		recipes.shapedCrafting("fertilizer_apatite", RecipeCategory.MISC, CoreItems.FERTILIZER_COMPOUND, 8, recipe -> {
			recipe.define('#', ItemTags.SAND);
			recipe.define('X', ForestryTags.Items.GEMS_APATITE);
			recipe.pattern(" # ");
			recipe.pattern(" X ");
			recipe.pattern(" # ");
		});

		recipes.shapedCrafting("fertilizer_ash", RecipeCategory.MISC, CoreItems.FERTILIZER_COMPOUND, 16, recipe -> {
			recipe.define('#', ForestryTags.Items.DUSTS_ASH);
			recipe.define('X', ForestryTags.Items.GEMS_APATITE);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});

		// Humus
		recipes.shapedCrafting("humus_compost", RecipeCategory.BUILDING_BLOCKS, CoreBlocks.HUMUS, 8, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', CoreItems.COMPOST);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});

		recipes.shapedCrafting("humus_fertilizer", RecipeCategory.BUILDING_BLOCKS, CoreBlocks.HUMUS, 8, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', CoreItems.FERTILIZER_COMPOUND);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});

		// Bog earth
		bogEarth(recipes, 8, CoreRecipes.getContainer(EnumContainerType.CAN, Fluids.WATER), "can");
		bogEarth(recipes, 8, CoreRecipes.getContainer(EnumContainerType.CAPSULE, Fluids.WATER), "wax_capsule");
		bogEarth(recipes, 8, CoreRecipes.getContainer(EnumContainerType.REFRACTORY, Fluids.WATER), "refractory");
		bogEarth(recipes, 6, Ingredient.of(Items.WATER_BUCKET), "bucket");
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

    static void registerMoistener(RecipeOutput output) {
        moistener(output, Items.MYCELIUM, Ingredient.of(Items.WHEAT_SEEDS), 5000);
        moistener(output, Items.MOSSY_COBBLESTONE, Ingredient.of(Items.COBBLESTONE), 20000);
        moistener(output, Items.MOSSY_STONE_BRICKS, Ingredient.of(Items.STONE_BRICKS), 20000);
        moistener(output, Items.PODZOL, Ingredient.of(Items.SPRUCE_LEAVES), 5000);
    }

    private static void moistener(RecipeOutput output, ItemLike result, Ingredient input, int time) {
        output.accept(CoreRecipes.id("moistener", path(result)), new MoistenerRecipe(time, input, new ItemStack(result)), null);
    }

	private static void bogEarth(MKRecipeProvider recipes, int amount, Ingredient container, String name) {
		recipes.shapedCrafting("bog_earth_" + name, RecipeCategory.BUILDING_BLOCKS, CoreBlocks.BOG_EARTH, amount, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', container);
			recipe.define('Y', ItemTags.SAND);
			recipe.pattern("#Y#");
			recipe.pattern("YXY");
			recipe.pattern("#Y#");
		});
	}
}
