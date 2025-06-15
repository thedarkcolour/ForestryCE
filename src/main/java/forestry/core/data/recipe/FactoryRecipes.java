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
import forestry.factory.recipes.FabricatorRecipe;
import forestry.factory.recipes.FabricatorSmeltingRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import thedarkcolour.modkit.data.MKRecipeProvider;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

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
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.IRON), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.INGOTS_IRON);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.INGOTS_GOLD);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.DIAMOND), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.GEMS_DIAMOND);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.OBSIDIAN), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Items.OBSIDIAN);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BLAZE), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Items.BLAZE_POWDER);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.EMERALD), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.GEMS_EMERALD);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.LAPIS), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Tags.Items.GEMS_LAPIS);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.ENDER), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Items.ENDER_EYE);
            recipe.define('X', Items.END_STONE);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.COPPER), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', Items.COPPER_INGOT);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.TIN), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', ForestryTags.Items.INGOTS_TIN);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BRONZE), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
        });
        fabricator(output, recipes, liquidGlass, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.APATITE), 4, recipe -> {
            recipe.pattern(" X ");
            recipe.pattern("#X#");
            recipe.pattern("XXX");
            recipe.define('#', Tags.Items.DUSTS_REDSTONE);
            recipe.define('X', ForestryTags.Items.GEMS_APATITE);
        });

        // Flexible casing
        fabricator(output, recipes, liquidGlass, CoreItems.FLEXIBLE_CASING, 1, recipe -> {
            recipe.pattern("#E#");
            recipe.pattern("B B");
            recipe.pattern("#E#");
            recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
            recipe.define('B', Tags.Items.SLIME_BALLS);
            recipe.define('E', Tags.Items.GEMS_EMERALD);
        });
    }

    private static void fabricator(RecipeOutput output, MKRecipeProvider recipes, @Nullable SizedFluidIngredient inputFluid, ItemLike result, int resultCount, Consumer<ShapedRecipeBuilder> pattern) {
        recipes.pushRecipeOutput(
            // the recipe is passed in by newOutput, letting us obtain the finished recipe instance from ModKit
            (id, recipe) -> output.accept(CoreRecipes.id("carpenter", MKRecipeProvider.path(result)), new FabricatorRecipe(Ingredient.EMPTY, Optional.ofNullable(inputFluid), (CraftingRecipe) recipe), null),
            // create a shaped recipe with the new output, which ModKit will pass into the above function
            newOutput -> recipes.shapedCrafting(RecipeCategory.MISC, result, resultCount, pattern)
        );
    }

    static void registerFabricatorSmelting(RecipeOutput consumer) {
        FluidStack liquidGlassBucket = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME);
        FluidStack liquidGlassX4 = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME * 4);
        FluidStack liquidGlass375 = ForestryFluids.GLASS.getFluid(375);

        fabricatorSmelting(consumer, "glass", Ingredient.of(Tags.Items.GLASS_BLOCKS_CHEAP), liquidGlassBucket, 1000);
        fabricatorSmelting(consumer, "glass_pane", Ingredient.of(Tags.Items.GLASS_PANES), liquidGlass375, 1000);
        fabricatorSmelting(consumer, "sand", Ingredient.of(Tags.Items.SANDS), liquidGlassBucket, 3000);
        fabricatorSmelting(consumer, "sandstone", Ingredient.of(Tags.Items.SANDSTONE_BLOCKS), liquidGlassX4, 4800);
    }

    private static void fabricatorSmelting(RecipeOutput output, String id, Ingredient input, FluidStack result, int meltingPoint) {
        output.accept(CoreRecipes.id("fabricator_smelting", id), new FabricatorSmeltingRecipe(input, result, meltingPoint), null);
    }
}
