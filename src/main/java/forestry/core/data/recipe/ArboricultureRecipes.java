package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.api.IForestryApi;
import forestry.api.arboriculture.IWoodAccess;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.VanillaWoodType;
import forestry.arboriculture.WoodAccess;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.fluids.ForestryFluids;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import thedarkcolour.modkit.data.MKRecipeProvider;

import java.util.List;

import static forestry.core.data.recipe.CoreRecipes.fabricator;

class ArboricultureRecipes {
	static void registerArboricultureRecipes(RecipeOutput output, MKRecipeProvider recipes) {
		registerWoodRecipes(recipes);

		for (ForestryWoodType type : ForestryWoodType.values()) {
			addFireproofRecipes(output, recipes, type);
		}

		for (VanillaWoodType type : VanillaWoodType.values()) {
			addFireproofRecipes(output, recipes, type);
		}

		recipes.shapedCrafting(RecipeCategory.TOOLS, ArboricultureItems.GRAFTER, recipe -> {
			recipe.define('B', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('#', Tags.Items.RODS_WOODEN);
			recipe.pattern("  B");
			recipe.pattern(" # ");
			recipe.pattern("#  ");
		});
		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST), recipe -> {
			recipe.define('X', ItemTags.SAPLINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.pattern(" # ");
			recipe.pattern("XYX");
			recipe.pattern("XXX");
		});
	}

	static void registerWoodRecipes(MKRecipeProvider recipes) {
		IWoodAccess woodAccess = WoodAccess.INSTANCE;
		List<IWoodType> woodTypes = woodAccess.getRegisteredWoodTypes();

		for (IWoodType woodType : woodTypes) {
			Block planks = woodAccess.getBlock(woodType, WoodBlockKind.PLANKS, false).getBlock();
			Block fireproofPlanks = woodAccess.getBlock(woodType, WoodBlockKind.PLANKS, true).getBlock();
			Block log = woodAccess.getBlock(woodType, WoodBlockKind.LOG, false).getBlock();
			Block fireproofLog = woodAccess.getBlock(woodType, WoodBlockKind.LOG, true).getBlock();
			Block wood = woodAccess.getBlock(woodType, WoodBlockKind.WOOD, false).getBlock();
			Block fireproofWood = woodAccess.getBlock(woodType, WoodBlockKind.WOOD, true).getBlock();
			Block strippedLog = woodAccess.getBlock(woodType, WoodBlockKind.STRIPPED_LOG, false).getBlock();
			Block fireproofStrippedLog = woodAccess.getBlock(woodType, WoodBlockKind.STRIPPED_LOG, true).getBlock();
			Block strippedWood = woodAccess.getBlock(woodType, WoodBlockKind.STRIPPED_WOOD, false).getBlock();
			Block fireproofStrippedWood = woodAccess.getBlock(woodType, WoodBlockKind.STRIPPED_WOOD, true).getBlock();
			Block door = woodAccess.getBlock(woodType, WoodBlockKind.DOOR, false).getBlock();
			Block trapdoor = woodAccess.getBlock(woodType, WoodBlockKind.TRAPDOOR, false).getBlock();
			Block fence = woodAccess.getBlock(woodType, WoodBlockKind.FENCE, false).getBlock();
			Block fireproofFence = woodAccess.getBlock(woodType, WoodBlockKind.FENCE, true).getBlock();
			Block fenceGate = woodAccess.getBlock(woodType, WoodBlockKind.FENCE_GATE, false).getBlock();
			Block fireproofFenceGate = woodAccess.getBlock(woodType, WoodBlockKind.FENCE_GATE, true).getBlock();
			Block slab = woodAccess.getBlock(woodType, WoodBlockKind.SLAB, false).getBlock();
			Block fireproofSlab = woodAccess.getBlock(woodType, WoodBlockKind.SLAB, true).getBlock();
			Block stairs = woodAccess.getBlock(woodType, WoodBlockKind.STAIRS, false).getBlock();
			Block fireproofStairs = woodAccess.getBlock(woodType, WoodBlockKind.STAIRS, true).getBlock();

			TagKey<Item> logTag = woodAccess.getLogItemTag(woodType, false);
			TagKey<Item> fireproofLogTag = woodAccess.getLogItemTag(woodType, true);

			recipes.woodenDoor(door, woodType instanceof VanillaWoodType ? Ingredient.of(fireproofPlanks) : Ingredient.of(planks, fireproofPlanks));

			// Regular (Forestry)
			if (woodType instanceof ForestryWoodType type) {
				makeCommonWoodenSet(recipes, planks, log, logTag, wood, strippedLog, strippedWood, fence, fenceGate, slab, stairs);

				recipes.shapelessCrafting(RecipeCategory.MISC, ArboricultureItems.CHEST_BOAT.item(type), 1, ArboricultureItems.BOAT.item(type), Tags.Items.CHESTS_WOODEN);
				recipes.shapedCrafting(RecipeCategory.MISC, ArboricultureItems.BOAT.item(type), recipe -> {
					recipe.define('P', Ingredient.of(planks, fireproofPlanks));
					recipe.pattern("P P");
					recipe.pattern("PPP");
				});

				recipes.woodenTrapdoor(trapdoor, Ingredient.of(planks, fireproofPlanks));

				recipes.shapedCrafting(RecipeCategory.MISC, ArboricultureBlocks.SIGN.get(type), recipe -> {
					recipe.define('P', Ingredient.of(planks, fireproofPlanks));
					recipe.define('S', Tags.Items.RODS_WOODEN);
					recipe.pattern("PPP");
					recipe.pattern("PPP");
					recipe.pattern(" S ");
				});

				recipes.shapedCrafting(RecipeCategory.MISC, ArboricultureBlocks.HANGING_SIGN.get(type), recipe -> {
					recipe.define('X', Items.CHAIN);
					recipe.define('#', Ingredient.of(strippedLog, fireproofStrippedLog));
					recipe.pattern("X X");
					recipe.pattern("###");
					recipe.pattern("###");
				});

				recipes.shapelessCrafting(RecipeCategory.REDSTONE, ArboricultureBlocks.BUTTON.get(type), 1, Ingredient.of(planks, fireproofPlanks));
				recipes.shapedCrafting(RecipeCategory.REDSTONE, ArboricultureBlocks.PRESSURE_PLATE.get(type), recipe -> {
					recipe.define('P', Ingredient.of(planks, fireproofPlanks));
					recipe.pattern("PP");
				});
			}

			// Fireproof (Vanilla & Forestry)
			makeCommonWoodenSet(recipes, fireproofPlanks, fireproofLog, fireproofLogTag, fireproofWood, fireproofStrippedLog, fireproofStrippedWood, fireproofFence, fireproofFenceGate, fireproofSlab, fireproofStairs);
		}
	}

	// Shared between regular and fireproof recipes
	static void makeCommonWoodenSet(MKRecipeProvider recipes, Block planks, Block log, TagKey<Item> logTag, Block wood, Block strippedLog, Block strippedWood, Block fence, Block fenceGate, Block slab, Block stairs) {
		recipes.shapelessCrafting(RecipeCategory.BUILDING_BLOCKS, planks, 4, "planks", logTag);
		recipes.woodenFence(fence, planks);
		recipes.woodenFenceGate(fenceGate, planks);
		recipes.woodenSlab(slab, planks);
		recipes.woodenStairs(stairs, planks);
		recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, wood, 3, Ingredient.of(log), "bark");
		recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, strippedWood, 3, Ingredient.of(strippedLog), "bark");
	}

	static void registerCharcoalRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.CHARCOAL.block(), recipe -> {
			recipe.define('#', Items.CHARCOAL);
			recipe.pattern("###");
			recipe.pattern("###");
			recipe.pattern("###");
		});

		recipes.shapelessCrafting("charcoal_from_block", RecipeCategory.MISC, Items.CHARCOAL, 9, ForestryTags.Items.CHARCOAL_BLOCK);

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.LOG_PILE.block(), recipe -> {
			recipe.define('L', ItemTags.LOGS);
			recipe.pattern(" L ");
			recipe.pattern("L L");
			recipe.pattern(" L ");
		});

		recipes.shapelessCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.DECORATIVE_LOG_PILE.block(), 1, CharcoalBlocks.LOG_PILE.block());

		recipes.shapelessCrafting("wood_pile_from_decorative", RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.LOG_PILE.block(), 1, CharcoalBlocks.DECORATIVE_LOG_PILE.block());
	}

	static void addFireproofRecipes(RecipeOutput consumer, MKRecipeProvider recipes, IWoodType type) {
		SizedFluidIngredient liquidGlass = ForestryFluids.GLASS.ingredient(500);

		List<WoodBlockKind> logLike = List.of(WoodBlockKind.LOG, WoodBlockKind.WOOD, WoodBlockKind.STRIPPED_LOG, WoodBlockKind.STRIPPED_WOOD);
		IWoodAccess woodAccess = IForestryApi.INSTANCE.getTreeManager().getWoodAccess();

		for (WoodBlockKind woodKind : logLike) {
			try {
				fabricator(consumer, recipes, liquidGlass, woodAccess.getBlock(type, woodKind, true), 2, recipe -> {
					recipe.pattern("   ");
					recipe.pattern("X#X");
					recipe.pattern("   ");
					recipe.define('#', CoreItems.REFRACTORY_WAX);
					recipe.define('X', woodAccess.getBlock(type, woodKind, false).getBlock()));
				});
			} catch (IllegalStateException ignored) {
			}
		}

		fabricator(consumer, recipes, liquidGlass, woodAccess.getBlock(type, WoodBlockKind.PLANKS, true), 8, recipe -> {
			recipe.pattern("XXX");
			recipe.pattern("X#X");
			recipe.pattern("XXX");
			recipe.define('#', CoreItems.REFRACTORY_WAX);
			recipe.define('X', woodAccess.getBlock(type, WoodBlockKind.PLANKS, false).getBlock()));
		});
	}
}
