package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.fluids.ForestryFluids;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.factory.blocks.BlockTypeFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import thedarkcolour.modkit.data.MKRecipeProvider;

class FactoryRecipes {
	static void registerFactoryRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.BOTTLER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', FluidsItems.CONTAINERS.get(EnumContainerType.CAN));
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CENTRIFUGE).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', Items.COPPER_INGOT);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', Tags.Items.INGOTS_GOLD);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.define('Z', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("XZX");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.FERMENTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.MOISTENER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', ForestryTags.Items.GEARS_COPPER);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.RAINMAKER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', ForestryTags.Items.GEARS_TIN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.RAINTANK).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', Tags.Items.INGOTS_IRON);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.SQUEEZER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', ForestryTags.Items.INGOTS_TIN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.STILL).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', Tags.Items.DUSTS_REDSTONE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});
	}

    static void registerFabricator(RecipeOutput output, MKRecipeProvider recipes) {
        SizedFluidIngredient liquidGlass = ForestryFluids.GLASS.ingredient(500);

        // Electron tubes
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.IRON), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.INGOTS_IRON);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.INGOTS_GOLD);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.DIAMOND), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.GEMS_DIAMOND);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.OBSIDIAN), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Items.OBSIDIAN);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BLAZE), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Items.BLAZE_POWDER);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.EMERALD), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.GEMS_EMERALD);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.LAPIS), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.GEMS_LAPIS);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.ENDER), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Items.ENDER_EYE);
            recipe.define('X', Items.END_STONE);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.COPPER), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Items.COPPER_INGOT);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.TIN), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', ForestryTags.Items.INGOTS_TIN);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BRONZE), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
        });
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.APATITE), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', ForestryTags.Items.GEMS_APATITE);
        });

        // Flexible casing
        CoreRecipes.fabricator(output, recipes, liquidGlass, CoreItems.FLEXIBLE_CASING, 1, recipe -> {
            recipe.pattern("#E#");
            recipe.pattern("B B");
            recipe.pattern("#E#");
            recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
            recipe.define('B', Tags.Items.SLIME_BALLS);
            recipe.define('E', Tags.Items.GEMS_EMERALD);
        });
    }

	static void registerFabricatorSmelting(RecipeOutput consumer) {
        FluidStack liquidGlassBucket = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME);
        FluidStack liquidGlassX4 = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME * 4);
        FluidStack liquidGlass375 = ForestryFluids.GLASS.getFluid(375);

        CoreRecipes.fabricatorSmelting(consumer, "glass", Ingredient.of(Tags.Items.GLASS_BLOCKS_CHEAP), liquidGlassBucket, 1000);
        CoreRecipes.fabricatorSmelting(consumer, "glass_pane", Ingredient.of(Tags.Items.GLASS_PANES), liquidGlass375, 1000);
        CoreRecipes.fabricatorSmelting(consumer, "sand", Ingredient.of(Tags.Items.SANDS), liquidGlassBucket, 3000);
        CoreRecipes.fabricatorSmelting(consumer, "sandstone", Ingredient.of(Tags.Items.SANDSTONE_BLOCKS), liquidGlassX4, 4800);
    }
}
