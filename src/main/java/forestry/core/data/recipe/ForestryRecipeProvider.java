package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.api.IForestryApi;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeManager;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.core.circuits.ICircuit;
import forestry.apiculture.features.ApicultureCrates;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.core.platform.block.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.bees.EnumHoneyComb;
import forestry.apiculture.bees.EnumPollenCluster;
import forestry.apiculture.bees.EnumPropolis;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.wood.VanillaWoodType;
import forestry.arboriculture.wood.WoodAccess;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.platform.block.BlockTypeCoreTesr;
import forestry.core.content.decorative.BlockTypeBigCandle;
import forestry.core.content.decorative.BlockTypeJumboCandle;
import forestry.core.content.decorative.BlockTypeMetalPlating;
import forestry.core.content.resources.EnumResourceType;
import forestry.core.engine.circuits.EnumCircuitBoardType;
import forestry.core.engine.circuits.ItemCircuitBoard;
import forestry.core.platform.config.Constants;
import forestry.core.platform.config.Preference;
import forestry.core.data.builder.*;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.item.FluidContainerType;
import forestry.core.content.resources.EnumCraftingMaterial;
import forestry.core.content.resources.EnumElectronTube;
import forestry.core.platform.util.ModUtil;
import forestry.core.platform.util.SpeciesUtil;
import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.blocks.BlockTypeFactoryTesr;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.platform.registration.FeatureItem;
import forestry.core.content.sorting.features.SortingBlocks;
import forestry.core.content.backpacks.features.BackpackItems;
import forestry.core.content.backpacks.features.CrateItems;
import forestry.core.content.backpacks.items.ItemCrated;
import forestry.core.content.worktable.features.WorktableBlocks;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import forestry.api.ForestryConstants;
import thedarkcolour.modkit.data.MKRecipeProvider;

import java.util.List;

import static forestry.core.data.recipe.RecipeIds.id;
import static thedarkcolour.modkit.data.MKRecipeProvider.ingredient;
import static thedarkcolour.modkit.data.MKRecipeProvider.path;

// todo split into smaller classes so that my computer doesn't die
public class ForestryRecipeProvider {
	public static final int STILL_DESTILLATION_DURATION = 100;
	public static final int STILL_DESTILLATION_INPUT = 10;
	public static final int STILL_DESTILLATION_OUTPUT = 3;

	public static ItemStack getContainer(FluidContainerType type, ForestryFluids fluid) {
		return getContainer(type, fluid.getFluid());
	}

	public static ItemStack getContainer(FluidContainerType type, Fluid fluid) {
		ItemStack container = FluidsItems.CONTAINERS.stack(type);
		return FluidUtil.getFluidHandler(container).map(handler -> {
			handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
			return container;
		}).orElse(ItemStack.EMPTY);
	}

	public static void addRecipes(RecipeOutput output, MKRecipeProvider recipes) {
		// Vanilla recipe types
		registerArboricultureRecipes(recipes);
		registerApicultureRecipes(recipes);
		registerFoodRecipes(recipes);
		registerBackpackRecipes(recipes);
		registerCharcoalRecipes(recipes);
		registerCoreRecipes(recipes);
		registerDecorativeRecipes(output, recipes);
		registerFactoryRecipes(recipes);
		registerFluidsRecipes(output);
		registerSortingRecipes(recipes);
		registerWorktableRecipes(recipes);
		registerEnergyRecipes(recipes);

		// Forestry recipe types
		registerCarpenter(output);
		registerCentrifuge(output);
		registerFabricator(output);
		registerFabricatorSmelting(output);
		registerFermenter(output);
		registerHygroregulator(output);
		registerMoistener(output);
		registerSqueezerContainer(output);
		registerSqueezer(output);
		registerSmelter(output);
		registerStill(output);

		// Built-in genetic mutations (bee/tree/butterfly) are generated as datapack recipes by the standalone
		// MutationProvider (registered in Data), which owns its own HashCache slice so removed mutation JSONs
		// are deleted rather than left orphaned.
	}

	private static void registerApicultureRecipes(MKRecipeProvider recipes) {
		registerCombRecipes(recipes);

		AlvearyBlock plain = ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.PLAIN).block();
		ItemLike goldElectronTube = CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD);

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, plain, recipe -> {
			recipe.define('X', CoreItems.IMPREGNATED_CASING);
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SCENTED_PANELING));
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.FAN).block(), recipe -> {
			recipe.define('#', goldElectronTube);
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("I I");
			recipe.pattern(" X ");
			recipe.pattern("I#I");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.HEATER).block(), recipe -> {
			recipe.define('#', goldElectronTube);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.define('X', plain);
			recipe.define('S', Tags.Items.STONES);
			recipe.pattern("#I#");
			recipe.pattern(" X ");
			recipe.pattern("SSS");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.HYGROREGULATOR).block(), recipe -> {
			recipe.define('G', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("GIG");
			recipe.pattern("GXG");
			recipe.pattern("GIG");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SIEVE).block(), recipe -> {
			recipe.define('W', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK));
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("III");
			recipe.pattern(" X ");
			recipe.pattern("WWW");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.STABILIZER).block(), recipe -> {
			recipe.define('X', plain);
			recipe.define('G', Tags.Items.GEMS_QUARTZ);
			recipe.pattern("G G");
			recipe.pattern("GXG");
			recipe.pattern("G G");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SWARMER).block(), recipe -> {
			recipe.define('#', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.DIAMOND));
			recipe.define('X', plain);
			recipe.define('G', Tags.Items.INGOTS_GOLD);
			recipe.pattern("#G#");
			recipe.pattern(" X ");
			recipe.pattern("#G#");
			recipe.group("alveary");
		});

		ItemLike wovenSilk = CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK);

		recipes.shapedCrafting(RecipeCategory.COMBAT, ApicultureItems.APIARIST_HELMET, recipe -> {
			recipe.define('#', wovenSilk);
			recipe.pattern("###");
			recipe.pattern("# #");
			recipe.group("apiarist_armour");
		});

		recipes.shapedCrafting(RecipeCategory.COMBAT, ApicultureItems.APIARIST_CHEST, recipe -> {
			recipe.define('#', wovenSilk);
			recipe.pattern("# #");
			recipe.pattern("###");
			recipe.pattern("###");
			recipe.group("apiarist_armour");
		});

		recipes.shapedCrafting(RecipeCategory.COMBAT, ApicultureItems.APIARIST_LEGS, recipe -> {
			recipe.define('#', wovenSilk);
			recipe.pattern("###");
			recipe.pattern("# #");
			recipe.pattern("# #");
			recipe.group("apiarist_armour");
		});

		recipes.shapedCrafting(RecipeCategory.COMBAT, ApicultureItems.APIARIST_BOOTS, recipe -> {
			recipe.define('#', wovenSilk);
			recipe.pattern("# #");
			recipe.pattern("# #");
			recipe.group("apiarist_armour");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureBlocks.BASE.get(ApicultureBlockType.APIARY).block(), recipe -> {
			recipe.define('S', ItemTags.WOODEN_SLABS);
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('C', CoreItems.IMPREGNATED_CASING);
			recipe.pattern("SSS");
			recipe.pattern("PCP");
			recipe.pattern("PPP");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureBlocks.BASE.get(ApicultureBlockType.BEE_HOUSE).block(), recipe -> {
			recipe.define('S', ItemTags.WOODEN_SLABS);
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('C', ForestryTags.Items.BEE_COMBS);
			recipe.pattern("SSS");
			recipe.pattern("PCP");
			recipe.pattern("PPP");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST), recipe -> {
			recipe.define('G', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', ForestryTags.Items.BEE_COMBS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern(" G ");
			recipe.pattern("XYX");
			recipe.pattern("XXX");
		});

		ItemLike propolis = ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL);

		recipes.shapedCrafting(RecipeCategory.MISC, CoreItems.BITUMINOUS_PEAT, recipe -> {
			recipe.define('#', ForestryTags.Items.DUSTS_ASH);
			recipe.define('X', CoreItems.PEAT);
			recipe.define('Y', propolis);
			recipe.pattern(" # ");
			recipe.pattern("XYX");
			recipe.pattern(" # ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureItems.FRAME_IMPREGNATED, recipe -> {
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.IMPREGNATED_STICK));
			recipe.define('S', Tags.Items.STRINGS);
			recipe.pattern("###");
			recipe.pattern("#S#");
			recipe.pattern("###");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureItems.FRAME_UNTREATED, recipe -> {
			recipe.define('#', Tags.Items.RODS_WOODEN);
			recipe.define('S', Tags.Items.STRINGS);
			recipe.pattern("###");
			recipe.pattern("#S#");
			recipe.pattern("###");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PULSATING_MESH), recipe -> {
			recipe.define('#', ApicultureItems.PROPOLIS.get(EnumPropolis.PULSATING));
			recipe.pattern("# #");
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SCOOP, recipe -> {
			recipe.define('#', Tags.Items.RODS_WOODEN);
			recipe.define('X', ItemTags.WOOL);
			recipe.pattern("#X#");
			recipe.pattern("###");
			recipe.pattern(" # ");
		});

		recipes.shapedCrafting("slime_from_propolis", RecipeCategory.MISC, Items.SLIME_BALL, recipe -> {
			recipe.define('#', propolis);
			recipe.define('X', ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.NORMAL));
			recipe.pattern("#X#");
			recipe.pattern("#X#");
			recipe.pattern("#X#");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, ApicultureItems.SMOKER, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_TIN);
			recipe.define('S', Tags.Items.RODS_WOODEN);
			recipe.define('F', Items.FLINT_AND_STEEL);
			recipe.define('L', Tags.Items.LEATHERS);
			recipe.pattern("LS#");
			recipe.pattern("LF#");
			recipe.pattern("###");
		});

		recipes.shapedCrafting("glistering_melon_slice", RecipeCategory.MISC, Items.GLISTERING_MELON_SLICE, recipe -> {
			recipe.define('#', CoreItems.HONEY_DROP);
			recipe.define('X', CoreItems.HONEYDEW);
			recipe.define('Y', Items.MELON_SLICE);
			recipe.pattern("#X#");
			recipe.pattern("#Y#");
			recipe.pattern("#X#");
		});

		ItemLike beesWax = CoreItems.BEESWAX;
		recipes.shapedCrafting("torch_from_wax", RecipeCategory.MISC, Items.TORCH, 3, recipe -> {
			recipe.define('#', beesWax);
			recipe.define('Y', Tags.Items.RODS_WOODEN);
			recipe.pattern(" # ");
			recipe.pattern(" # ");
			recipe.pattern(" Y ");
		});

		// Deviation from 1.20.1: storage3x3 replaces that tree's grid3x3 plus a hand-named
		// "uncraft_wax_block". It writes the same two recipes and names the reverse one
		// beeswax_from_wax_block, matching ash_from_ash_block and ingot_tin_from_resource_storage_tin
		recipes.storage3x3(ApicultureBlocks.WAX_BLOCK.item(), CoreItems.BEESWAX);
		recipes.storage3x3(ApicultureBlocks.REFRACTORY_WAX_BLOCK.item(), CoreItems.REFRACTORY_WAX);

		// The bricks use ash or wood pulp to stop them melting. That's the logic.
		// Deviation from 1.20.1: those recipe ids said "wax_bricks_ash", which named the brick block
		// rather than the brick item. Renamed to the <result>_from_<binder> form this tree already uses
		recipes.shapedCrafting("wax_brick_from_ash", RecipeCategory.BUILDING_BLOCKS, CoreItems.WAX_BRICK, 2, recipe -> {
			recipe.define('X', CoreItems.BEESWAX);
			recipe.define('#', ForestryTags.Items.DUSTS_ASH);
			recipe.pattern("#X");
			recipe.pattern("X#");
		});
		recipes.shapedCrafting("wax_brick_from_sawdust", RecipeCategory.BUILDING_BLOCKS, CoreItems.WAX_BRICK, 2, recipe -> {
			recipe.define('X', CoreItems.BEESWAX);
			recipe.define('#', ForestryTags.Items.SAWDUST);
			recipe.pattern("#X");
			recipe.pattern("X#");
		});
		recipes.shapedCrafting("refractory_wax_brick_from_ash", RecipeCategory.BUILDING_BLOCKS, CoreItems.REFRACTORY_WAX_BRICK, 2, recipe -> {
			recipe.define('X', CoreItems.REFRACTORY_WAX);
			recipe.define('#', ForestryTags.Items.DUSTS_ASH);
			recipe.pattern("#X");
			recipe.pattern("X#");
		});
		recipes.shapedCrafting("refractory_wax_brick_from_sawdust", RecipeCategory.BUILDING_BLOCKS, CoreItems.REFRACTORY_WAX_BRICK, 2, recipe -> {
			recipe.define('X', CoreItems.REFRACTORY_WAX);
			recipe.define('#', ForestryTags.Items.SAWDUST);
			recipe.pattern("#X");
			recipe.pattern("X#");
		});
	}

	private static void registerCombRecipes(MKRecipeProvider recipes) {
		for (EnumHoneyComb honeyComb : EnumHoneyComb.VALUES) {
			ItemLike comb = ApicultureItems.BEE_COMBS.get(honeyComb);
			Block combBlock = ApicultureBlocks.COMB_BLOCK.get(honeyComb).block();
			recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, combBlock, 1, Ingredient.of(comb), "combs");
		}
	}

	private static void registerArboricultureRecipes(MKRecipeProvider recipes) {
		registerWoodRecipes(recipes);

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
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.pattern(" # ");
			recipe.pattern("XYX");
			recipe.pattern("XXX");
		});
	}

	private static void registerWoodRecipes(MKRecipeProvider recipes) {
		WoodAccess woodAccess = WoodAccess.INSTANCE;
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
	private static void makeCommonWoodenSet(MKRecipeProvider recipes, Block planks, Block log, TagKey<Item> logTag, Block wood, Block strippedLog, Block strippedWood, Block fence, Block fenceGate, Block slab, Block stairs) {
		recipes.shapelessCrafting(RecipeCategory.BUILDING_BLOCKS, planks, 4, "planks", logTag);
		recipes.woodenFence(fence, planks);
		recipes.woodenFenceGate(fenceGate, planks);
		recipes.woodenSlab(slab, planks);
		recipes.woodenStairs(stairs, planks);
		recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, wood, 3, Ingredient.of(log), "bark");
		recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, strippedWood, 3, Ingredient.of(strippedLog), "bark");
	}

	private static void registerFoodRecipes(MKRecipeProvider recipes) {
		ItemLike waxCapsule = FluidsItems.CONTAINERS.get(FluidContainerType.WAX_CAPSULE);
		ItemLike honeyDrop = CoreItems.HONEY_DROP;

		recipes.shapedCrafting(RecipeCategory.FOOD, ApicultureItems.AMBROSIA, recipe -> {
			recipe.define('#', CoreItems.HONEYDEW);
			recipe.define('X', ApicultureItems.ROYAL_JELLY);
			recipe.define('Y', waxCapsule);
			recipe.pattern("#Y#");
			recipe.pattern("XXX");
			recipe.pattern("###");
		});

		recipes.shapedCrafting(RecipeCategory.FOOD, ApicultureItems.HONEYED_SLICE, recipe -> {
			recipe.define('#', honeyDrop);
			recipe.define('X', Items.BREAD);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});

		recipes.shapelessCrafting("bottled_honey_drops", RecipeCategory.FOOD, Items.HONEY_BOTTLE, 1, Items.GLASS_BOTTLE, honeyDrop, honeyDrop);
	}

	private static void registerBackpackRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.ADVENTURER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.BONES);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.BREWER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Items.GLASS_BOTTLE);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.BUILDER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Items.CLAY_BALL);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.DIGGER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.STONES);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.FORESTER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', ItemTags.LOGS);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.HUNTER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.FEATHERS);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.MINER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.INGOTS_IRON);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		// Naturalist backpacks
		naturalistBackpack(recipes, BackpackItems.APIARIST_BACKPACK, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST));
		naturalistBackpack(recipes, BackpackItems.LEPIDOPTERIST_BACKPACK, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST));
		naturalistBackpack(recipes, BackpackItems.ARBORIST_BACKPACK, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST));
	}

	private static void naturalistBackpack(MKRecipeProvider recipes, ItemLike backpack, ItemLike chest) {
		recipes.shapedCrafting(RecipeCategory.TOOLS, backpack, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.RODS_WOODEN);
			recipe.define('X', Tags.Items.STRINGS);
			recipe.define('Y', chest);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});
	}

	private static void registerCharcoalRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.CHARCOAL.block(), recipe -> {
			recipe.define('#', Items.CHARCOAL);
			recipe.pattern("###");
			recipe.pattern("###");
			recipe.pattern("###");
		});

		// todo custom IDs
		recipes.shapelessCrafting("charcoal_from_block", RecipeCategory.MISC, Items.CHARCOAL, 9, ForestryTags.Items.CHARCOAL_BLOCK);

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.LOG_PILE.block(), recipe -> {
			recipe.define('L', ItemTags.LOGS);
			recipe.pattern(" L ");
			recipe.pattern("L L");
			recipe.pattern(" L ");
		});

		recipes.shapelessCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.DECORATIVE_LOG_PILE.block(), 1, CharcoalBlocks.LOG_PILE.block());

		recipes.shapelessCrafting("wood_pile_from_decorative", RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.LOG_PILE.block(), 1, CharcoalBlocks.DECORATIVE_LOG_PILE.block());

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.ASH.item(), recipe -> {
			recipe.define('X', ForestryTags.Items.DUSTS_ASH);
			recipe.pattern("XX");
			recipe.pattern("XX");
		});
		// name collides with the peat->ash smelting recipe (would otherwise overwrite one of the recipes)
		recipes.shapelessCrafting("ash_from_ash_block", RecipeCategory.MISC, CoreItems.ASH, 4, CharcoalBlocks.ASH.item());
	}

	private static void registerCoreRecipes(MKRecipeProvider recipes) {
		recipes.oreSmelting(ingredient(CoreBlocks.APATITE_ORE.get(), CoreBlocks.DEEPSLATE_APATITE_ORE.get()), CoreItems.APATITE, 0.5f, 200);
		recipes.oreSmelting(ingredient(CoreBlocks.TIN_ORE.get(), CoreBlocks.DEEPSLATE_TIN_ORE.get(), CoreItems.RAW_TIN), CoreItems.TIN_INGOT, 0.5f, 200);
		recipes.smelting(Ingredient.of(CoreItems.PEAT.item()), CoreItems.ASH, 0.0f, 200);
		recipes.storage3x3(CoreBlocks.RAW_TIN_BLOCK, CoreItems.RAW_TIN);

		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER), recipe -> {
			recipe.define('T', CoreItems.PORTABLE_ANALYZER);
			recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("XTX");
			recipe.pattern(" Y ");
			recipe.pattern("X X");
		});
		recipes.storage3x3(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.APATITE), CoreItems.APATITE);
		recipes.storage3x3(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.BRONZE), CoreItems.BRONZE_INGOT);
		// Tin storage block crafted from any tin ingot (forge:ingots/tin tag) so cross-mod
		// tin from Mekanism, Railcraft, etc. is accepted. Decomposition still produces
		// Forestry's specific tin ingot.
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.TIN), recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_TIN);
			recipe.pattern("###");
			recipe.pattern("###");
			recipe.pattern("###");
		});
		recipes.shapelessCrafting("ingot_tin_from_resource_storage_tin", RecipeCategory.MISC, CoreItems.TIN_INGOT.item(), 9, CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.TIN));
		// Deviation from 1.20.1: that tree spelled the counted ingredient IntObjectPair.of(9, item);
		// this tree's idiom for the same MKRecipeProvider feature is ObjectIntPair.of(item, 9)
		recipes.shapelessCrafting("tin_from_nuggets", RecipeCategory.MISC, CoreItems.TIN_INGOT, 1, ObjectIntPair.of(CoreItems.TIN_NUGGET, 9));
		recipes.shapelessCrafting(RecipeCategory.MISC, CoreItems.TIN_NUGGET, 9, CoreItems.TIN_INGOT);

		// Building blocks
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.TURF, 3, recipe -> {
			recipe.define('X', CoreBlocks.TURF_BLOCK);
			recipe.pattern("XX");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.TIN_CHAIN, recipe -> {
			recipe.define('|', Ingredient.of(ForestryTags.Items.INGOTS_TIN));
			recipe.define('.', Ingredient.of(ForestryTags.Items.NUGGETS_TIN));
			recipe.pattern(".");
			recipe.pattern("|");
			recipe.pattern(".");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreItems.PHOSPHOR_TORCH_ITEM, 4, recipe -> {
			recipe.define('.', CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.PHOSPHOR));
			recipe.define('^', ItemTags.COALS);
			recipe.define('|', Items.STICK);
			recipe.pattern(".");
			recipe.pattern("^");
			recipe.pattern("|");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.PHOSPHOR_LANTERN, recipe -> {
			recipe.define('.', Ingredient.of(ForestryTags.Items.NUGGETS_TIN));
			recipe.define('^', CoreItems.PHOSPHOR_TORCH_ITEM);
			recipe.pattern("...");
			recipe.pattern(".^.");
			recipe.pattern("...");
		});
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.AMBER), recipe -> {
			recipe.define('#', CoreItems.AMBER);
			recipe.pattern("##");
			recipe.pattern("##");
		});
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.SILICON), recipe -> {
			recipe.define('#', CoreItems.SILICON);
			recipe.pattern("##");
			recipe.pattern("##");
		});
		recipes.shapelessCrafting(RecipeCategory.MISC, CoreItems.SILICON, 4, CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.SILICON));
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SURVIVALISTS_PICKAXE, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern("###");
			recipe.pattern(" X ");
			recipe.pattern(" X ");
		});
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SURVIVALISTS_SHOVEL, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern(" # ");
			recipe.pattern(" X ");
			recipe.pattern(" X ");
		});
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SURVIVALISTS_AXE, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern("## ");
			recipe.pattern("#X ");
			recipe.pattern(" X ");
		});
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SURVIVALISTS_SWORD, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern(" # ");
			recipe.pattern(" # ");
			recipe.pattern(" X ");
		});
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SURVIVALISTS_HOE, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern("## ");
			recipe.pattern(" X ");
			recipe.pattern(" X ");
		});

		gear(recipes, CoreItems.GEAR_BRONZE, ForestryTags.Items.INGOTS_BRONZE);
		gear(recipes, CoreItems.GEAR_TIN, ForestryTags.Items.INGOTS_TIN);
		gear(recipes, CoreItems.GEAR_COPPER, Tags.Items.INGOTS_COPPER);
		gear(recipes, CoreItems.GEAR_IRON, Tags.Items.INGOTS_IRON);

		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.PICKAXE_KIT, 1, CoreItems.SURVIVALISTS_PICKAXE, CoreItems.CARTON);
		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.SHOVEL_KIT, 1, CoreItems.SURVIVALISTS_SHOVEL, CoreItems.CARTON);
		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.AXE_KIT, 1, CoreItems.SURVIVALISTS_AXE, CoreItems.CARTON);
		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.SWORD_KIT, 1, CoreItems.SURVIVALISTS_SWORD, CoreItems.CARTON);
		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.HOE_KIT, 1, CoreItems.SURVIVALISTS_HOE, CoreItems.CARTON);
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.SPECTACLES, recipe -> {
			recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('Y', Tags.Items.GLASS_PANES);
			recipe.pattern(" X ");
			recipe.pattern("Y Y");
		});
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.PIPETTE, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('X', Tags.Items.GLASS_PANES);
			recipe.pattern("  #");
			recipe.pattern(" X ");
			recipe.pattern("X  ");
		});

		recipes.shapedCrafting("string_from_wisp", RecipeCategory.MISC, Items.STRING, recipe -> {
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP));
			recipe.pattern(" # ");
			recipe.pattern(" # ");
			recipe.pattern(" # ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, CoreItems.STURDY_CASING, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.pattern("###");
			recipe.pattern("# #");
			recipe.pattern("###");
		});

		recipes.shapedCrafting("cobweb_from_wisp", RecipeCategory.MISC, Items.COBWEB, 4, recipe -> {
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP));
			recipe.pattern("# #");
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.WRENCH, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.pattern("# #");
			recipe.pattern(" # ");
			recipe.pattern(" # ");
		});

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
		bogRecipe(recipes, 8, getContainer(FluidContainerType.CAN, Fluids.WATER), "can");
		bogRecipe(recipes, 8, getContainer(FluidContainerType.WAX_CAPSULE, Fluids.WATER), "wax_capsule");
		bogRecipe(recipes, 8, getContainer(FluidContainerType.REFRACTORY_CAPSULE, Fluids.WATER), "refractory");
		bogRecipe(recipes, 6, new ItemStack(Items.WATER_BUCKET), "bucket");

		recipes.shapedCrafting("can", RecipeCategory.MISC, FluidsItems.CONTAINERS.get(FluidContainerType.CAN), 12, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_TIN);
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting("capsule", RecipeCategory.MISC, FluidsItems.CONTAINERS.get(FluidContainerType.WAX_CAPSULE), 4, recipe -> {
			recipe.define('#', CoreItems.BEESWAX);
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting("refractory_capsule", RecipeCategory.MISC, FluidsItems.CONTAINERS.get(FluidContainerType.REFRACTORY_CAPSULE), 4, recipe -> {
			recipe.define('#', CoreItems.REFRACTORY_WAX);
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting("compressed_ice_shards", RecipeCategory.MISC, Items.ICE, 1, recipe -> {
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD));
			recipe.pattern("##");
			recipe.pattern("##");
		});

		recipes.shapedCrafting("honey_drop_block", RecipeCategory.MISC, Items.HONEY_BLOCK, 1, recipe -> {
			recipe.define('V', CoreItems.HONEY_DROP);
			recipe.pattern("VVV");
			recipe.pattern("V V");
			recipe.pattern("VVV");
		});

		recipes.shapedCrafting("beeswax_candles", RecipeCategory.MISC, Items.CANDLE, 1, recipe -> {
			recipe.define('|', Tags.Items.STRINGS);
			recipe.define('^', CoreItems.BEESWAX);
			recipe.pattern(" | ");
			recipe.pattern(" ^ ");
		});

		// Explicit ID, carried over from 1.20.1, which named this recipe in the plural like the one above it
		recipes.shapedCrafting("refractory_candles", RecipeCategory.MISC, CoreBlocks.REFRACTORY_CANDLE, 1, recipe -> {
			recipe.define('|', Tags.Items.STRINGS);
			recipe.define('^', CoreItems.REFRACTORY_WAX);
			recipe.pattern(" | ");
			recipe.pattern(" ^ ");
		});

		// Plywood. 1.20.1 also carried a commented-out sheet recipe, noting the carpenter was the better
		// place for it. The carpenter has it, so the comment is dropped rather than carried over
		recipes.grid3x3(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.PLYWOOD_BLOCK, Ingredient.of(CoreBlocks.PLYWOOD_SHEET));
		recipes.shapelessCrafting("plywood_from_block", RecipeCategory.BUILDING_BLOCKS, CoreBlocks.PLYWOOD_SHEET, 9, CoreBlocks.PLYWOOD_BLOCK);

		// Books
		recipes.shapelessCrafting("foresters_manual_honeydrop", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, CoreItems.HONEY_DROP);
		recipes.shapelessCrafting("foresters_manual_sapling", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, ItemTags.SAPLINGS);
	}

	/**
	 * Registers the crafting, smelting and stonecutting recipes of the decorative stone and brick blocks.
	 */
	private static void registerDecorativeRecipes(RecipeOutput output, MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.ASH_BRICKS.base(), recipe -> {
			recipe.define('X', CoreItems.ASH_BRICK);
			recipe.pattern("XX");
			recipe.pattern("XX");
		});
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.WAX_BRICKS.base(), 4, recipe -> {
			recipe.define('X', CoreItems.WAX_BRICK);
			recipe.pattern("XX");
			recipe.pattern("XX");
		});
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.REFRACTORY_WAX_BRICKS.base(), 4, recipe -> {
			recipe.define('X', CoreItems.REFRACTORY_WAX_BRICK);
			recipe.pattern("XX");
			recipe.pattern("XX");
		});

		stoneFamily(output, recipes, CoreBlocks.ASH_BRICKS);
		chiseledStone(output, recipes, CoreBlocks.CHISELED_ASH_BRICKS, CoreBlocks.ASH_BRICKS);
		stoneFamily(output, recipes, CoreBlocks.WAX_BRICKS);
		chiseledStone(output, recipes, CoreBlocks.CHISELED_WAX_BRICKS, CoreBlocks.WAX_BRICKS);
		stoneFamily(output, recipes, CoreBlocks.REFRACTORY_WAX_BRICKS);
		chiseledStone(output, recipes, CoreBlocks.CHISELED_REFRACTORY_WAX_BRICKS, CoreBlocks.REFRACTORY_WAX_BRICKS);

		stoneSet(output, recipes, CoreBlocks.WAXSTONE, Ingredient.of(CoreItems.BEESWAX));
		stoneSet(output, recipes, CoreBlocks.REFRACTORY_WAXSTONE, Ingredient.of(CoreItems.REFRACTORY_WAX));
		stoneSet(output, recipes, CoreBlocks.HONEYSTONE, Ingredient.of(CoreItems.HONEY_DROP, CoreItems.HONEYDEW));

		registerCandleRecipes(recipes);
	}

	/**
	 * Registers the crafting recipes of the jumbo and big candles. The plain and refractory candles are cast
	 * from wax, and every other colour is one of those dyed.
	 */
	private static void registerCandleRecipes(MKRecipeProvider recipes) {
		bigCandle(recipes, BlockTypeBigCandle.NORMAL, CoreItems.BEESWAX);
		jumboCandle(recipes, BlockTypeJumboCandle.NORMAL, ApicultureBlocks.WAX_BLOCK);
		bigCandle(recipes, BlockTypeBigCandle.REFRACTORY, CoreItems.REFRACTORY_WAX);
		jumboCandle(recipes, BlockTypeJumboCandle.REFRACTORY, ApicultureBlocks.REFRACTORY_WAX_BLOCK);

		// The rainbow candles stay uncraftable, as on 1.20.1, where the comment reads "it's fun to have secrets"
		for (BlockTypeJumboCandle type : BlockTypeJumboCandle.values()) {
			TagKey<Item> dye = type.getDye();
			if (dye != null) {
				// Deviation from 1.20.1: the jumbo recipes took ForestryTags.Items.BIG_CANDLES as their base
				// there, so a jumbo candle was dyed out of a big one. Read as a copy-paste slip from the big
				// candle recipe beside it, and given the jumbo tag
				recipes.shapelessCrafting(RecipeCategory.DECORATIONS, CoreBlocks.JUMBO_CANDLES.get(type), 1, ForestryTags.Items.JUMBO_CANDLES, Ingredient.of(dye));
			}
		}
		for (BlockTypeBigCandle type : BlockTypeBigCandle.values()) {
			TagKey<Item> dye = type.getDye();
			if (dye != null) {
				recipes.shapelessCrafting(RecipeCategory.DECORATIONS, CoreBlocks.BIG_CANDLES.get(type), 1, ForestryTags.Items.BIG_CANDLES, Ingredient.of(dye));
			}
		}
	}

	/**
	 * Registers the crafting recipe of one big candle cast from wax, which is six wax under one string.
	 *
	 * @param recipes The provider the recipe is written through
	 * @param type    The candle the recipe yields
	 * @param wax     The wax the candle is cast from
	 */
	private static void bigCandle(MKRecipeProvider recipes, BlockTypeBigCandle type, ItemLike wax) {
		recipes.shapedCrafting(RecipeCategory.DECORATIONS, CoreBlocks.BIG_CANDLES.get(type), recipe -> {
			recipe.define('|', Tags.Items.STRINGS);
			recipe.define('#', wax);
			recipe.pattern(" | ");
			recipe.pattern("###");
			recipe.pattern("###");
		});
	}

	/**
	 * Registers the crafting recipe of one jumbo candle cast from wax, which is one wax block under one string.
	 *
	 * @param recipes The provider the recipe is written through
	 * @param type    The candle the recipe yields
	 * @param wax     The wax block the candle is cast from
	 */
	private static void jumboCandle(MKRecipeProvider recipes, BlockTypeJumboCandle type, ItemLike wax) {
		recipes.shapedCrafting(RecipeCategory.DECORATIONS, CoreBlocks.JUMBO_CANDLES.get(type), recipe -> {
			recipe.define('|', Tags.Items.STRINGS);
			recipe.define('#', wax);
			recipe.pattern("|");
			recipe.pattern("#");
		});
	}

	/**
	 * Registers the recipes of one decorative stone set. The plain stone and the cobbled block are eight
	 * vanilla stone or cobblestone soaked in one unit of the binder, the brick and polished finishes are cut
	 * from those two, and the plain stone is also smelted back out of the cobbled block.
	 *
	 * @param output  The output the stonecutting recipes are written through
	 * @param recipes The provider the crafting and smelting recipes are written through
	 * @param set     The seventeen blocks to write recipes for
	 * @param binder  The ingredient the vanilla stone is soaked in, one per eight
	 */
	private static void stoneSet(RecipeOutput output, MKRecipeProvider recipes, CoreBlocks.StoneSet set, Ingredient binder) {
		ItemLike stone = set.stone().base();
		ItemLike cobbled = set.cobbled().base();

		// Explicit ID: the default would be the plain stone's own name, which the smelting recipe below
		// already claims, and one would silently overwrite the other
		recipes.shapedCrafting(path(stone) + "_crafting", RecipeCategory.BUILDING_BLOCKS, stone, 8, recipe -> {
			recipe.define('X', binder);
			recipe.define('#', Blocks.STONE);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, cobbled, 8, recipe -> {
			recipe.define('X', binder);
			recipe.define('#', Blocks.COBBLESTONE);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});
		recipes.smelting(cobbled, stone, 0.1f);

		recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, set.polished().base(), 4, Ingredient.of(cobbled));
		recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, set.bricks().base(), 4, Ingredient.of(stone));

		for (CoreBlocks.StoneFamily family : set.families()) {
			stoneFamily(output, recipes, family);
		}
		chiseledStone(output, recipes, set.chiseled(), set.stone());
	}

	private static void stoneFamily(RecipeOutput output, MKRecipeProvider recipes, CoreBlocks.StoneFamily family) {
		ItemLike base = family.base();
		String name = path(base);

		recipes.stairs(family.stairs(), base);
		stonecutting(output, base, family.stairs(), 1, name + "_stairs_from_stonecutting");

		recipes.slab(family.slab(), base);
		stonecutting(output, base, family.slab(), 2, name + "_slabs_from_stonecutting");

		recipes.grid3x2(RecipeCategory.BUILDING_BLOCKS, family.wall(), 6, Ingredient.of(base));
		stonecutting(output, base, family.wall(), 1, name + "_walls_from_stonecutting");
	}

	private static void chiseledStone(RecipeOutput output, MKRecipeProvider recipes, ItemLike chiseled, CoreBlocks.StoneFamily family) {
		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, chiseled, recipe -> {
			recipe.define('_', family.slab());
			recipe.pattern("_");
			recipe.pattern("_");
		});
		stonecutting(output, family.base(), chiseled, 1, path(chiseled) + "_from_stonecutting");
	}

	private static void stonecutting(RecipeOutput output, ItemLike input, ItemLike result, int count, String name) {
		SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count);
		MKRecipeProvider.unlockedByHaving(builder, input);
		builder.save(output, id(name));
	}

	private static void bogRecipe(MKRecipeProvider recipes, int amount, ItemStack container, String name) {
		recipes.shapedCrafting("bog_earth_" + name, RecipeCategory.BUILDING_BLOCKS, CoreBlocks.BOG_EARTH, amount, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', DataComponentIngredient.of(true, container));
			recipe.define('Y', ItemTags.SAND);
			recipe.pattern("#Y#");
			recipe.pattern("YXY");
			recipe.pattern("#Y#");
		});
	}

	private static void gear(MKRecipeProvider recipes, ItemLike gear, TagKey<Item> ingot) {
		// In old versions, these gears were upgrades of BuildCraft's stone gears (which are tiered)
		// Might bring this back if anything comes out of that BuildCraft port.
		// For now, just have the same recipes as Thermal.
		recipes.shapedCrafting(RecipeCategory.MISC, gear, recipe -> {
			recipe.define('#', ingot);
			recipe.define('X', Tags.Items.NUGGETS_IRON);
			recipe.pattern(" # ");
			recipe.pattern("#X#");
			recipe.pattern(" # ");
		});
	}

	private static void registerFactoryRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.BOTTLER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', FluidsItems.CONTAINERS.get(FluidContainerType.CAN));
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CARPENTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CENTRIFUGE).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', Tags.Items.INGOTS_COPPER);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', Tags.Items.INGOTS_GOLD);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.define('Z', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("XZX");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FERMENTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.MOISTENER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', ForestryTags.Items.GEARS_COPPER);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.RAINMAKER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', ForestryTags.Items.GEARS_TIN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		// Deviation from 1.20.1: '#' was Tags.Items.GLASS there, which every sibling above resolves to
		// GLASS_BLOCKS_COLORLESS in this tree
		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.SMELTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', Tags.Items.INGOTS_IRON);
			recipe.define('Y', Items.FURNACE);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.BURN_BARREL.block(), recipe -> {
			recipe.define('#', Items.IRON_BARS);
			recipe.define('X', Tags.Items.INGOTS_IRON);
			recipe.pattern("X X");
			recipe.pattern("X#X");
			recipe.pattern("XXX");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.SQUEEZER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', ForestryTags.Items.INGOTS_TIN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.STILL).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('X', Tags.Items.DUSTS_REDSTONE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});
	}

	private static void registerFluidsRecipes(RecipeOutput output) {
		// Bypass MKRecipeProvider's shapedCrafting wrapper here: its
		// attemptAutoCriterion calls Ingredient#getValues, which throws on
		// DataComponentIngredient. Build with vanilla ShapedRecipeBuilder
		// and set the unlock criterion via MKRecipeProvider.unlockedByHaving.
		for (FluidContainerType containerType : FluidContainerType.values()) {
			MKRecipeProvider.unlockedByHaving(
				ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, Items.CAKE)
					.define('A', DataComponentIngredient.of(true, getContainer(containerType, NeoForgeMod.MILK.get())))
					.define('B', Items.SUGAR)
					.define('C', Items.WHEAT)
					.define('E', Items.EGG)
					.pattern("AAA")
					.pattern("BEB")
					.pattern("CCC"),
				Items.MILK_BUCKET
			).save(output, ForestryConstants.forestry("cake_" + containerType.getSerializedName()));
		}
	}

	private static void registerSortingRecipes(MKRecipeProvider recipes) {
		// Named as a tag rather than as the two items, because the filter is a core machine and those
		// items live in apiculture and lepidopterology. An absent item fails the recipe to parse; an
		// absent tag just resolves empty, so this crafts with whichever jars are installed
		Ingredient ing = CompoundIngredient.of(
			Ingredient.of(ForestryTags.Items.GENETIC_SAMPLES),
			Ingredient.of(ForestryTags.Items.FORESTRY_FRUITS)
		);

		recipes.shapedCrafting(RecipeCategory.MISC, SortingBlocks.FILTER.block(), 2, recipe -> {
			recipe.define('B', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('D', Tags.Items.GEMS_DIAMOND);
			recipe.define('F', ing);
			recipe.define('W', ItemTags.PLANKS);
			recipe.define('G', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.pattern("WDW");
			recipe.pattern("FGF");
			recipe.pattern("BDB");
		});
	}

	private static void registerWorktableRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, WorktableBlocks.WORKTABLE.block(), recipe -> {
			recipe.define('B', Items.BOOK);
			recipe.define('T', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES);
			recipe.define('C', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("B");
			recipe.pattern("T");
			recipe.pattern("C");
		});
	}

	private static void registerEnergyRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK), recipe -> {
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('I', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('Q', ForestryTags.Items.GEARS_COPPER);
			recipe.define('D', Items.PISTON);
			recipe.define('C', Items.CLOCK);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDC");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS), recipe -> {
			recipe.define('P', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('I', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('Q', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.PEAT), recipe -> {
			recipe.define('P', Tags.Items.INGOTS_COPPER);
			recipe.define('I', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('Q', ForestryTags.Items.GEARS_COPPER);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});

		// Deviation from 1.20.1: Tags.Items.GLASS is now GLASS_BLOCKS_COLORLESS, matching the other engines
		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.COMBUSTION), recipe -> {
			recipe.define('P', Tags.Items.INGOTS_IRON);
			recipe.define('I', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('Q', ForestryTags.Items.GEARS_IRON);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});

		// Deviation from 1.20.1: Tags.Items.GLASS is now GLASS_BLOCKS_COLORLESS, matching the other engines
		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.SOLAR), recipe -> {
			recipe.define('P', ForestryTags.Items.INGOTS_TIN);
			recipe.define('I', Tags.Items.GLASS_BLOCKS_COLORLESS);
			recipe.define('Q', ForestryTags.Items.GEARS_TIN);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.SOLAR_PANEL, recipe -> {
			recipe.define('P', Items.GLASS_PANE);
			recipe.define('S', CoreItems.SOLAR_CELL);
			recipe.define('Q', ForestryTags.Items.INGOTS_TIN);
			recipe.define('D', CoreItems.ELECTRON_TUBES.item(EnumElectronTube.COPPER));
			recipe.pattern("PPP");
			recipe.pattern("SSS");
			recipe.pattern("QDQ");
		});
	}

	private static void registerCarpenter(RecipeOutput consumer) {
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.SEED_OIL.getFluid(250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.IMPREGNATED_CASING)
				.pattern("###")
				.pattern("# #")
				.pattern("###")
				.define('#', ItemTags.LOGS))
			.build(consumer, id("carpenter", "impregnated_casing"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.SEED_OIL.getFluid(500))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).item())
				.pattern("#  ")
				.pattern("###")
				.pattern("# #")
				.define('#', ItemTags.PLANKS))
			.build(consumer, id("carpenter", "escritoire"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.SEED_OIL.getFluid(100))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.IMPREGNATED_STICK), 2)
				.pattern("#")
				.pattern("#")
				.define('#', ItemTags.LOGS))
			.build(consumer, id("carpenter", "impregnated_stick"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), 4)
				.requires(ItemTags.LOGS))
			.build(consumer, id("carpenter", "wood_pulp"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreBlocks.HUMUS, 9)
				.pattern("###")
				.pattern("#X#")
				.pattern("###")
				.define('#', Items.DIRT)
				.define('X', CoreItems.MULCH))
			.build(consumer, id("carpenter", "humus"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreBlocks.BOG_EARTH, 8)
				.pattern("#X#")
				.pattern("XYX")
				.pattern("#X#")
				.define('#', Items.DIRT)
				.define('X', Tags.Items.SANDS)
				.define('Y', CoreItems.MULCH))
			.build(consumer, id("carpenter", "bog_earth"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(75)
			.setLiquid(new FluidStack(Fluids.WATER, 5000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.HARDENED_CASING)
				.pattern("X X")
				.pattern(" Y ")
				.pattern("X X")
				.define('X', Tags.Items.GEMS_DIAMOND)
				.define('Y', CoreItems.STURDY_CASING))
			.build(consumer, id("carpenter", "hardened_casing"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.IODINE_CHARGE)
				.pattern("Z#Z")
				.pattern("#Y#")
				.pattern("X#X")
				.define('#', ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.NORMAL))
				.define('X', Items.GUNPOWDER)
				.define('Y', FluidsItems.CONTAINERS.get(FluidContainerType.CAN))
				.define('Z', CoreItems.HONEY_DROP))
			.build(consumer, id("carpenter", "iodine_charge"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.DISSIPATION_CHARGE)
				.pattern("Z#Z")
				.pattern("#Y#")
				.pattern("X#X")
				.define('#', ApicultureItems.ROYAL_JELLY)
				.define('X', Items.GUNPOWDER)
				.define('Y', FluidsItems.CONTAINERS.get(FluidContainerType.CAN))
				.define('Z', CoreItems.HONEYDEW))
			.build(consumer, id("carpenter", "dissipation_charge"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(100)
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.ENDER_PEARL)
				.pattern(" # ")
				.pattern("###")
				.pattern(" # ")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PULSATING_MESH)))
			.build(consumer, id("carpenter", "ender_pearl"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(10)
			.setLiquid(new FluidStack(Fluids.WATER, 500))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK))
				.pattern("XX")
				.pattern("XX")
				.define('X', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP)))
			.build(consumer, id("carpenter", "woven_silk"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.BRONZE_INGOT, 2)
				.requires(CoreItems.BROKEN_SURVIVALISTS_PICKAXE))
			.build(consumer, id("carpenter", "reclaim_bronze_pickaxe"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.BRONZE_INGOT, 1)
				.requires(CoreItems.BROKEN_SURVIVALISTS_SHOVEL))
			.build(consumer, id("carpenter", "reclaim_bronze_shovel"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.BRONZE_INGOT, 2)
				.requires(CoreItems.BROKEN_SURVIVALISTS_AXE))
			.build(consumer, id("carpenter", "reclaim_bronze_axe"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.BRONZE_INGOT, 1)
				.requires(CoreItems.BROKEN_SURVIVALISTS_SWORD))
			.build(consumer, id("carpenter", "reclaim_bronze_sword"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.BRONZE_INGOT, 1)
				.requires(CoreItems.BROKEN_SURVIVALISTS_HOE))
			.build(consumer, id("carpenter", "reclaim_bronze_hoe"));
		// todo conditional recipe for Create honey fluid 1.20
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.HONEY.getFluid(500))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SCENTED_PANELING))
				.pattern(" J ")
				.pattern("###")
				.pattern("WPW")
				.define('#', ItemTags.PLANKS)
				.define('J', ApicultureItems.ROYAL_JELLY)
				.define('W', CoreItems.BEESWAX)
				.define('P', ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.NORMAL)))
			.build(consumer, id("carpenter", "scented_paneling"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(100)
			.setLiquid(new FluidStack(Fluids.WATER, 2000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.PORTABLE_ANALYZER)
				.pattern("X#X")
				.pattern("X#X")
				.pattern("RDR")
				.define('#', Tags.Items.GLASS_PANES)
				.define('X', ForestryTags.Items.INGOTS_TIN)
				.define('R', Tags.Items.DUSTS_REDSTONE)
				.define('D', Tags.Items.GEMS_DIAMOND))
			.build(consumer, id("carpenter", "portable_analyzer"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.PICKAXE_KIT)
				.pattern("###")
				.pattern(" X ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(consumer, id("carpenter", "kit_pickaxe"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.SHOVEL_KIT)
				.pattern(" # ")
				.pattern(" X ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(consumer, id("carpenter", "kit_shovel"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.AXE_KIT)
				.pattern("## ")
				.pattern("#X ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(consumer, id("carpenter", "kit_axe"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.SWORD_KIT)
				.pattern(" # ")
				.pattern(" # ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(consumer, id("carpenter", "kit_sword"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.HOE_KIT)
				.pattern("## ")
				.pattern(" X ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(consumer, id("carpenter", "kit_hoe"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(40)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.SOLDERING_IRON)
				.pattern(" # ")
				.pattern("# #")
				.pattern("  B")
				.define('#', Tags.Items.INGOTS_IRON)
				.define('B', ForestryTags.Items.INGOTS_BRONZE))
			.build(consumer, id("carpenter", "soldering_iron"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER)
				.pattern("#")
				.pattern("#")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOOD_PULP)))
			.build(consumer, id("carpenter", "paper"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CARTON, 2)
				.pattern(" # ")
				.pattern("# #")
				.pattern(" # ")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOOD_PULP)))
			.build(consumer, id("carpenter", "carton"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(5)
			.setLiquid(new FluidStack(Fluids.WATER, 50))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ASH_BRICK, 1)
				.pattern("##")
				.define('#', CoreItems.ASH.item()))
			.build(consumer, id("carpenter", "ash_brick"));

		ItemStack basic = ItemCircuitBoard.createCircuitboard(EnumCircuitBoardType.BASIC, null, new ICircuit[]{});
		ItemStack enhanced = ItemCircuitBoard.createCircuitboard(EnumCircuitBoardType.ENHANCED, null, new ICircuit[]{});
		ItemStack refined = ItemCircuitBoard.createCircuitboard(EnumCircuitBoardType.REFINED, null, new ICircuit[]{});
		ItemStack intricate = ItemCircuitBoard.createCircuitboard(EnumCircuitBoardType.INTRICATE, null, new ICircuit[]{});

		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.override(basic)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.BASIC))
				.pattern("R R")
				.pattern("R#R")
				.pattern("R R")
				.define('#', ForestryTags.Items.INGOTS_TIN)
				.define('R', Tags.Items.DUSTS_REDSTONE))
			.build(consumer, id("carpenter", "circuits", "basic"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(40)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.override(enhanced)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.ENHANCED))
				.pattern("R#R")
				.pattern("R#R")
				.pattern("R#R")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('R', Tags.Items.DUSTS_REDSTONE))
			.build(consumer, id("carpenter", "circuits", "enhanced"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(80)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.override(refined)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.REFINED))
				.pattern("R#R")
				.pattern("R#R")
				.pattern("R#R")
				.define('#', Tags.Items.INGOTS_IRON)
				.define('R', Tags.Items.DUSTS_REDSTONE))
			.build(consumer, id("carpenter", "circuits", "refined"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(80)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.override(intricate)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.INTRICATE))
				.pattern("R#R")
				.pattern("R#R")
				.pattern("R#R")
				.define('#', Tags.Items.INGOTS_GOLD)
				.define('R', Tags.Items.DUSTS_REDSTONE))
			.build(consumer, id("carpenter", "circuits", "intricate"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.CANDLE, 4)
				.pattern("# #")
				.pattern(" X ")
				.pattern("# #")
				.define('#', CoreItems.BEESWAX)
				.define('X', Items.STRING))
			.build(consumer, id("carpenter", "candles"));

		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setLiquid(new FluidStack(Fluids.WATER, 100))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, CoreBlocks.TURF_BLOCK, 4)
				.pattern("XX")
				.pattern("XX")
				.define('X', Blocks.GRASS_BLOCK))
			.build(consumer, id("carpenter", "turf_blocks"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 100))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.PLYWOOD_SHEET, 24)
				.pattern("___")
				.pattern("^^^")
				.define('_', ItemTags.WOODEN_SLABS)
				.define('^', ForestryTags.Items.SAWDUST))
			.build(consumer, id("carpenter", "plywood"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 200))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.CORK, 4)
				.pattern("**")
				.pattern("**")
				.define('*', ForestryTags.Items.SAWDUST))
			.build(consumer, id("carpenter", "cork"));

		// Crates
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CrateItems.CRATE, 24)
				.pattern(" # ")
				.pattern("# #")
				.pattern(" # ")
				.define('#', ItemTags.LOGS))
			.build(consumer, id("carpenter", "crates", "empty"));

		crate(consumer, CrateItems.CRATED_PEAT.get(), Ingredient.of(CoreItems.PEAT));
		crate(consumer, CrateItems.CRATED_APATITE.get(), Ingredient.of(ForestryTags.Items.GEMS_APATITE));
		crate(consumer, CrateItems.CRATED_FERTILIZER_COMPOUND.get(), Ingredient.of(CoreItems.FERTILIZER_COMPOUND));
		crate(consumer, CrateItems.CRATED_MULCH.get(), Ingredient.of(CoreItems.MULCH));
		crate(consumer, CrateItems.CRATED_PHOSPHOR.get(), Ingredient.of(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.PHOSPHOR)));
		crate(consumer, CrateItems.CRATED_ASH.get(), Ingredient.of(CoreItems.ASH));
		crate(consumer, CrateItems.CRATED_TIN.get(), Ingredient.of(ForestryTags.Items.INGOTS_TIN));
		crate(consumer, CrateItems.CRATED_COPPER.get(), Ingredient.of(Tags.Items.INGOTS_COPPER));
		crate(consumer, CrateItems.CRATED_BRONZE.get(), Ingredient.of(ForestryTags.Items.INGOTS_BRONZE));

		crate(consumer, CrateItems.CRATED_HUMUS.get(), Ingredient.of(CoreBlocks.HUMUS));
		crate(consumer, CrateItems.CRATED_BOG_EARTH.get(), Ingredient.of(CoreBlocks.BOG_EARTH));

		crate(consumer, CrateItems.CRATED_WHEAT.get(), Ingredient.of(Tags.Items.CROPS_WHEAT));
		crate(consumer, CrateItems.CRATED_COOKIE.get(), Ingredient.of(Items.COOKIE));
		crate(consumer, CrateItems.CRATED_REDSTONE.get(), Ingredient.of(Tags.Items.DUSTS_REDSTONE));
		crate(consumer, CrateItems.CRATED_LAPIS.get(), Ingredient.of(Tags.Items.GEMS_LAPIS));
		crate(consumer, CrateItems.CRATED_SUGAR_CANE.get(), Ingredient.of(Items.SUGAR_CANE));
		crate(consumer, CrateItems.CRATED_CLAY_BALL.get(), Ingredient.of(Items.CLAY_BALL));
		crate(consumer, CrateItems.CRATED_GLOWSTONE.get(), Ingredient.of(Tags.Items.DUSTS_GLOWSTONE));
		crate(consumer, CrateItems.CRATED_APPLE.get(), Ingredient.of(Items.APPLE));
		crate(consumer, CrateItems.CRATED_COAL.get(), Ingredient.of(Items.COAL));
		crate(consumer, CrateItems.CRATED_CHARCOAL.get(), Ingredient.of(Items.CHARCOAL));
		crate(consumer, CrateItems.CRATED_SEEDS.get(), Ingredient.of(Items.WHEAT_SEEDS));
		crate(consumer, CrateItems.CRATED_POTATO.get(), Ingredient.of(Tags.Items.CROPS_POTATO));
		crate(consumer, CrateItems.CRATED_CARROT.get(), Ingredient.of(Tags.Items.CROPS_CARROT));
		crate(consumer, CrateItems.CRATED_BEETROOT.get(), Ingredient.of(Tags.Items.CROPS_BEETROOT));
		crate(consumer, CrateItems.CRATED_NETHER_WART.get(), Ingredient.of(Tags.Items.CROPS_NETHER_WART));

		crate(consumer, CrateItems.CRATED_OAK_LOG.get(), Ingredient.of(Items.OAK_LOG));
		crate(consumer, CrateItems.CRATED_BIRCH_LOG.get(), Ingredient.of(Items.BIRCH_LOG));
		crate(consumer, CrateItems.CRATED_JUNGLE_LOG.get(), Ingredient.of(Items.JUNGLE_LOG));
		crate(consumer, CrateItems.CRATED_SPRUCE_LOG.get(), Ingredient.of(Items.SPRUCE_LOG));
		crate(consumer, CrateItems.CRATED_ACACIA_LOG.get(), Ingredient.of(Items.ACACIA_LOG));
		crate(consumer, CrateItems.CRATED_DARK_OAK_LOG.get(), Ingredient.of(Items.DARK_OAK_LOG));
		crate(consumer, CrateItems.CRATED_COBBLESTONE.get(), Ingredient.of(Tags.Items.COBBLESTONES));
		crate(consumer, CrateItems.CRATED_DIRT.get(), Ingredient.of(Items.DIRT));
		crate(consumer, CrateItems.CRATED_GRASS_BLOCK.get(), Ingredient.of(Items.GRASS_BLOCK));
		crate(consumer, CrateItems.CRATED_STONE.get(), Ingredient.of(Items.STONE));
		crate(consumer, CrateItems.CRATED_GRANITE.get(), Ingredient.of(Items.GRANITE));
		crate(consumer, CrateItems.CRATED_DIORITE.get(), Ingredient.of(Items.DIORITE));
		crate(consumer, CrateItems.CRATED_ANDESITE.get(), Ingredient.of(Items.ANDESITE));
		crate(consumer, CrateItems.CRATED_PRISMARINE.get(), Ingredient.of(Items.PRISMARINE));
		crate(consumer, CrateItems.CRATED_PRISMARINE_BRICKS.get(), Ingredient.of(Items.PRISMARINE_BRICKS));
		crate(consumer, CrateItems.CRATED_DARK_PRISMARINE.get(), Ingredient.of(Items.DARK_PRISMARINE));
		crate(consumer, CrateItems.CRATED_BRICKS.get(), Ingredient.of(Items.BRICKS));
		crate(consumer, CrateItems.CRATED_CACTUS.get(), Ingredient.of(Items.CACTUS));
		crate(consumer, CrateItems.CRATED_SAND.get(), Ingredient.of(Items.SAND));
		crate(consumer, CrateItems.CRATED_RED_SAND.get(), Ingredient.of(Items.RED_SAND));
		crate(consumer, CrateItems.CRATED_OBSIDIAN.get(), Ingredient.of(Tags.Items.OBSIDIANS));
		crate(consumer, CrateItems.CRATED_NETHERRACK.get(), Ingredient.of(Tags.Items.NETHERRACKS));
		crate(consumer, CrateItems.CRATED_SOUL_SAND.get(), Ingredient.of(Items.SOUL_SAND));
		crate(consumer, CrateItems.CRATED_SANDSTONE.get(), Ingredient.of(Tags.Items.SANDSTONE_BLOCKS));
		crate(consumer, CrateItems.CRATED_NETHER_BRICKS.get(), Ingredient.of(Items.NETHER_BRICKS));
		crate(consumer, CrateItems.CRATED_MYCELIUM.get(), Ingredient.of(Items.MYCELIUM));
		crate(consumer, CrateItems.CRATED_GRAVEL.get(), Ingredient.of(Tags.Items.GRAVELS));
		crate(consumer, CrateItems.CRATED_OAK_SAPLING.get(), Ingredient.of(Items.OAK_SAPLING));
		crate(consumer, CrateItems.CRATED_BIRCH_SAPLING.get(), Ingredient.of(Items.BIRCH_SAPLING));
		crate(consumer, CrateItems.CRATED_JUNGLE_SAPLING.get(), Ingredient.of(Items.JUNGLE_SAPLING));
		crate(consumer, CrateItems.CRATED_SPRUCE_SAPLING.get(), Ingredient.of(Items.SPRUCE_SAPLING));
		crate(consumer, CrateItems.CRATED_ACACIA_SAPLING.get(), Ingredient.of(Items.ACACIA_SAPLING));
		crate(consumer, CrateItems.CRATED_DARK_OAK_SAPLING.get(), Ingredient.of(Items.DARK_OAK_SAPLING));

		crate(consumer, CrateItems.CRATED_BEESWAX.get(), Ingredient.of(CoreItems.BEESWAX));
		crate(consumer, CrateItems.CRATED_REFRACTORY_WAX.get(), Ingredient.of(CoreItems.REFRACTORY_WAX));

		crate(consumer, ApicultureCrates.CRATED_POLLEN_CLUSTER_NORMAL.get(), Ingredient.of(ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.NORMAL)));
		crate(consumer, ApicultureCrates.CRATED_POLLEN_CLUSTER_CRYSTALLINE.get(), Ingredient.of(ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.CRYSTALLINE)));
		crate(consumer, ApicultureCrates.CRATED_PROPOLIS.get(), Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL)));
		crate(consumer, CrateItems.CRATED_HONEYDEW.get(), Ingredient.of(CoreItems.HONEYDEW));
		crate(consumer, ApicultureCrates.CRATED_ROYAL_JELLY.get(), Ingredient.of(ApicultureItems.ROYAL_JELLY));

		for (EnumHoneyComb comb : EnumHoneyComb.VALUES) {
			crate(consumer, ApicultureCrates.CRATED_BEE_COMBS.get(comb).get(), Ingredient.of(ApicultureItems.BEE_COMBS.get(comb)));
		}

		wovenBackpack(consumer, "miner", BackpackItems.MINER_BACKPACK, BackpackItems.MINER_BACKPACK_T_2);
		wovenBackpack(consumer, "digger", BackpackItems.DIGGER_BACKPACK, BackpackItems.DIGGER_BACKPACK_T_2);
		wovenBackpack(consumer, "forester", BackpackItems.FORESTER_BACKPACK, BackpackItems.FORESTER_BACKPACK_T_2);
		wovenBackpack(consumer, "hunter", BackpackItems.HUNTER_BACKPACK, BackpackItems.HUNTER_BACKPACK_T_2);
		wovenBackpack(consumer, "adventurer", BackpackItems.ADVENTURER_BACKPACK, BackpackItems.ADVENTURER_BACKPACK_T_2);
		wovenBackpack(consumer, "builder", BackpackItems.BUILDER_BACKPACK, BackpackItems.BUILDER_BACKPACK_T_2);
		wovenBackpack(consumer, "brewer", BackpackItems.BREWER_BACKPACK, BackpackItems.BREWER_BACKPACK_T_2);
	}

	private static void wovenBackpack(RecipeOutput consumer, String id, FeatureItem<?> tier1, FeatureItem<?> tier2) {
		new CarpenterRecipeBuilder()
			.setPackagingTime(200)
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, tier2)
				.pattern("WXW")
				.pattern("WTW")
				.pattern("WWW")
				.define('W', CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.WOVEN_SILK).getItem())
				.define('X', Items.DIAMOND)
				.define('T', tier1))
			.build(consumer, id("woven_backpack", id));
	}

	private static void crate(RecipeOutput consumer, ItemCrated crated, Ingredient ingredient) {
		ItemStack contained = crated.getContained();
		ResourceLocation name = ModUtil.getRegistryName(contained.getItem());

		new CarpenterRecipeBuilder()
			.setPackagingTime(Constants.CARPENTER_CRATING_CYCLES)
			.setLiquid(new FluidStack(Fluids.WATER, Constants.CARPENTER_CRATING_LIQUID_QUANTITY))
			.setBox(Ingredient.of(CrateItems.CRATE))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, crated, 1)
				.pattern("###")
				.pattern("###")
				.pattern("###")
				.define('#', ingredient))
			.build(consumer, id("carpenter", "crates", "pack", name.getNamespace(), name.getPath()));
		new CarpenterRecipeBuilder()
			.setPackagingTime(Constants.CARPENTER_UNCRATING_CYCLES)
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, contained.getItem(), 9).requires(crated))
			.build(consumer, id("carpenter", "crates", "unpack", name.getNamespace(), name.getPath()));
	}

	private static void registerCentrifuge(RecipeOutput consumer) {

		ItemStack honeyDrop = CoreItems.HONEY_DROP.stack();
		ItemStack magmaDrop = ApicultureItems.MAGMATIC_DROP.stack();

		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.HONEY)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.9F, honeyDrop)
			.build(consumer, id("centrifuge", "honey_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.COCOA)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.5f, new ItemStack(Items.COCOA_BEANS))
			.build(consumer, id("centrifuge", "cocoa_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.SIMMERING)))
			.product(1.0f, CoreItems.REFRACTORY_WAX.stack())
			.product(0.7f, magmaDrop)
			.build(consumer, id("centrifuge", "simmering_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.STRINGY)))
			.product(1.0f, ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1))
			.product(0.4f, honeyDrop)
			.build(consumer, id("centrifuge", "stringy_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.DRIPPING)))
			.product(1.0f, CoreItems.HONEYDEW.stack())
			.product(0.4f, honeyDrop)
			.build(consumer, id("centrifuge", "dripping_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.FROZEN)))
			.product(0.8f, CoreItems.BEESWAX.stack())
			.product(0.7f, honeyDrop)
			.product(0.4f, new ItemStack(Items.SNOWBALL))
			.product(0.2f, ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.CRYSTALLINE, 1))
			.build(consumer, id("centrifuge", "frozen_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.SILKY)))
			.product(1.0f, honeyDrop)
			.product(0.8f, ApicultureItems.PROPOLIS.stack(EnumPropolis.SILKY, 1))
			.build(consumer, id("centrifuge", "silky_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.PARCHED)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.9f, honeyDrop)
			.build(consumer, id("centrifuge", "parched_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.MYSTERIOUS)))
			.product(1.0f, ApicultureItems.PROPOLIS.stack(EnumPropolis.PULSATING, 1))
			.product(0.4f, honeyDrop)
			.build(consumer, id("centrifuge", "mysterious_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.POWDERY)))
			.product(0.2f, honeyDrop)
			.product(0.2f, CoreItems.BEESWAX.stack())
			.product(0.9f, new ItemStack(Items.GUNPOWDER))
			.build(consumer, id("centrifuge", "powdery_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.WHEATEN)))
			.product(0.2f, honeyDrop)
			.product(0.2f, CoreItems.BEESWAX.stack())
			.product(0.8f, new ItemStack(Items.WHEAT))
			.build(consumer, id("centrifuge", "wheaten_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.MOSSY)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.9f, honeyDrop)
			.build(consumer, id("centrifuge", "mossy_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.KAOLIN)))
			.product(1.0f, new ItemStack(Items.CLAY_BALL))
			.product(0.9f, honeyDrop)
			.build(consumer, id("centrifuge", "kaolin_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.MELLOW)))
			.product(0.6f, CoreItems.HONEYDEW.stack())
			.product(0.2f, CoreItems.BEESWAX.stack())
			.product(0.3f, new ItemStack(Items.QUARTZ))
			.build(consumer, id("centrifuge", "mellow_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.VINTAGE)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.9f, CoreItems.HONEYDEW.stack())
			.build(consumer, id("centrifuge", "vintage_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.SCULKEN)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.9f, ApicultureItems.EXPERIENCE_DROP.stack())
			.product(0.2F, new ItemStack(Items.SCULK))
			.build(consumer, id("centrifuge", "sculken_comb"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(5)
			.setInput(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.SILKY)))
			.product(0.6f, CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.SILK_WISP, 1))
			.product(0.1f, ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1))
			.build(consumer, id("centrifuge", "silky_propolis"));

		new CentrifugeRecipeBuilder()
			.setProcessingTime(180)
			.setInput(Ingredient.of(ArboricultureItems.AMBER_SAPLING_FOSSIL))
			.product(0.25f, SpeciesUtil.TREE_TYPE.get().createStack(ForestryTreeSpecies.GINKGO, TreeLifeStage.SAPLING))
			.product(0.8f, CoreItems.AMBER.stack())
			.build(consumer, id("centrifuge", "amber_sapling"));
		new CentrifugeRecipeBuilder()
			.setProcessingTime(180)
			.setInput(Ingredient.of(ApicultureItems.AMBER_DRONE))
			.product(0.25f, SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.CHRONOFUGE, BeeLifeStage.DRONE))
			.product(0.8f, CoreItems.AMBER.stack())
			.build(consumer, id("centrifuge", "amber_drone"));

		new CentrifugeRecipeBuilder()
			.setProcessingTime(20)
			.setInput(Ingredient.of(Items.HONEYCOMB))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.build(consumer, id("centrifuge", "comb_to_wax"));
	}

	private static void registerFabricator(RecipeOutput consumer) {
		FluidStack liquidGlass = ForestryFluids.GLASS.getFluid(500);

		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.IRON), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Tags.Items.INGOTS_IRON))
			.build(consumer, id("fabricator", "electron_tubes", "iron"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Tags.Items.INGOTS_GOLD))
			.build(consumer, id("fabricator", "electron_tubes", "gold"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.DIAMOND), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Tags.Items.GEMS_DIAMOND))
			.build(consumer, id("fabricator", "electron_tubes", "diamond"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.OBSIDIAN), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Items.OBSIDIAN))
			.build(consumer, id("fabricator", "electron_tubes", "obsidian"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BLAZE), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Items.BLAZE_POWDER))
			.build(consumer, id("fabricator", "electron_tubes", "blaze"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.EMERALD), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Tags.Items.GEMS_EMERALD))
			.build(consumer, id("fabricator", "electron_tubes", "emerald"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.LAPIS), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Tags.Items.GEMS_LAPIS))
			.build(consumer, id("fabricator", "electron_tubes", "lapis"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.ENDER), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Items.ENDER_EYE)
				.define('X', Items.END_STONE))
			.build(consumer, id("fabricator", "electron_tubes", "ender"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.COPPER), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', Tags.Items.INGOTS_COPPER))
			.build(consumer, id("fabricator", "electron_tubes", "copper"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.TIN), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', ForestryTags.Items.INGOTS_TIN))
			.build(consumer, id("fabricator", "electron_tubes", "tin"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BRONZE), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', ForestryTags.Items.INGOTS_BRONZE))
			.build(consumer, id("fabricator", "electron_tubes", "bronze"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.APATITE), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', ForestryTags.Items.GEMS_APATITE))
			.build(consumer, id("fabricator", "electron_tubes", "apatite"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.AMBER), 2)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', ForestryTags.Items.GEMS_AMBER))
			.build(consumer, id("fabricator", "electron_tubes", "amber"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.ELECTRON_TUBES.get(EnumElectronTube.SILICON), 4)
				.pattern(" X ")
				.pattern("#X#")
				.pattern("XXX")
				.define('#', Tags.Items.DUSTS_REDSTONE)
				.define('X', ForestryTags.Items.SILICON))
			.build(consumer, id("fabricator", "electron_tubes", "silicon"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(ForestryFluids.GLASS.getFluid(50))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.SOLAR_CELL)
				.pattern(" T ")
				.pattern("LSL")
				.pattern(" ^ ")
				.define('S', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHORESCENT_JELLY))
				.define('^', ForestryTags.Items.SILICON)
				.define('L', Tags.Items.GEMS_LAPIS)
				.define('T', ForestryTags.Items.NUGGETS_TIN))
			.build(consumer, id("fabricator", "solar_cell"));
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.FLEXIBLE_CASING)
				.pattern("#E#")
				.pattern("B B")
				.pattern("#E#")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('B', Tags.Items.SLIMEBALLS)
				.define('E', Tags.Items.GEMS_EMERALD))
			.build(consumer, id("fabricator", "electron_tubes", "flexible_casing"));

		metalPlating(consumer, BlockTypeMetalPlating.IRON, Items.IRON_INGOT);
		metalPlating(consumer, BlockTypeMetalPlating.GOLD, Items.GOLD_INGOT);
		metalPlating(consumer, BlockTypeMetalPlating.COPPER, Items.COPPER_INGOT);
		metalPlating(consumer, BlockTypeMetalPlating.NETHERITE, Items.NETHERITE_INGOT);
		metalPlating(consumer, BlockTypeMetalPlating.TIN, CoreItems.TIN_INGOT);
		// Deviation from 1.20.1: the bronze recipe named the tin plating as its result there, so bronze
		// ingots made tin plating and the bronze plating had no recipe at all. It yields bronze here
		metalPlating(consumer, BlockTypeMetalPlating.BRONZE, CoreItems.BRONZE_INGOT);

		for (BlockTypeMetalPlating type : BlockTypeMetalPlating.values()) {
			TagKey<Item> dye = type.getDye();
			if (dye != null) {
				lacqueredMetalPlating(consumer, type, dye);
			}
		}

		for (ForestryWoodType type : ForestryWoodType.values()) {
			addFireproofRecipes(consumer, type);
		}

		for (VanillaWoodType type : VanillaWoodType.values()) {
			addFireproofRecipes(consumer, type);
		}
	}

	private static void metalPlating(RecipeOutput consumer, BlockTypeMetalPlating type, ItemLike base) {
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(ForestryFluids.WAX.getFluid(50))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.METAL_PLATING.get(type), 8)
				.pattern("###")
				.pattern("# #")
				.pattern("###")
				.define('#', base))
			.build(consumer, id("metal_plating", type.getName()));
	}

	private static void lacqueredMetalPlating(RecipeOutput consumer, BlockTypeMetalPlating type, TagKey<Item> dye) {
		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(ForestryFluids.WAX.getFluid(50))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, CoreBlocks.METAL_PLATING.get(type), 8)
				.pattern("###")
				.pattern("#D#")
				.pattern("###")
				.define('D', Ingredient.of(dye))
				.define('#', Ingredient.of(ForestryTags.Items.METAL_PLATING)))
			.build(consumer, id("metal_plating", type.getName()));
	}

	private static void addFireproofRecipes(RecipeOutput consumer, IWoodType type) {
		FluidStack liquidGlass = ForestryFluids.GLASS.getFluid(500);

		List<WoodBlockKind> logLike = List.of(WoodBlockKind.LOG, WoodBlockKind.WOOD, WoodBlockKind.STRIPPED_LOG, WoodBlockKind.STRIPPED_WOOD);
		ITreeManager woodAccess = IForestryApi.INSTANCE.getTreeManager();

		for (WoodBlockKind woodKind : logLike) {
			try {
				new FabricatorRecipeBuilder()
					.setPlan(Ingredient.EMPTY)
					.setMolten(liquidGlass)
					.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, woodAccess.getBlock(type, woodKind, true).getBlock(), 2)
						.pattern("   ")
						.pattern("X#X")
						.pattern("   ")
						.define('#', CoreItems.REFRACTORY_WAX)
						.define('X', woodAccess.getBlock(type, woodKind, false).getBlock()))
					.build(consumer, id("fabricator", "fireproof", woodKind.getSerializedName(), type.toString()));
			} catch (IllegalStateException ignored) {
			}
		}

		new FabricatorRecipeBuilder()
			.setPlan(Ingredient.EMPTY)
			.setMolten(liquidGlass)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, woodAccess.getBlock(type, WoodBlockKind.PLANKS, true).getBlock(), 8)
				.pattern("XXX")
				.pattern("X#X")
				.pattern("XXX")
				.define('#', CoreItems.REFRACTORY_WAX)
				.define('X', woodAccess.getBlock(type, WoodBlockKind.PLANKS, false).getBlock()))
			.build(consumer, id("fabricator", "fireproof", "planks", type.toString()));
	}

	private static void registerFabricatorSmelting(RecipeOutput consumer) {
		FluidStack liquidGlassBucket = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME);
		FluidStack liquidGlassX4 = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME * 4);
		FluidStack liquidGlass375 = ForestryFluids.GLASS.getFluid(375);

		new FabricatorSmeltingRecipeBuilder()
			.setResource(Ingredient.of(Items.GLASS))
			.setProduct(liquidGlassBucket)
			.setMeltingPoint(1000)
			.build(consumer, id("fabricator", "smelting", "glass"));
		new FabricatorSmeltingRecipeBuilder()
			.setResource(Ingredient.of(Items.GLASS_PANE))
			.setProduct(liquidGlass375)
			.setMeltingPoint(1000)
			.build(consumer, id("fabricator", "smelting", "glass_pane"));
		new FabricatorSmeltingRecipeBuilder()
			.setResource(Ingredient.of(Items.SAND, Items.RED_SAND))
			.setProduct(liquidGlassBucket)
			.setMeltingPoint(3000)
			.build(consumer, id("fabricator", "smelting", "sand"));
		new FabricatorSmeltingRecipeBuilder()
			.setResource(Ingredient.of(Items.SANDSTONE, Items.SMOOTH_SANDSTONE, Items.CHISELED_SANDSTONE))
			.setProduct(liquidGlassX4)
			.setMeltingPoint(4800)
			.build(consumer, id("fabricator", "smelting", "sandstone"));

		new FabricatorSmeltingRecipeBuilder()
			.setResource(Ingredient.of(ApicultureBlocks.WAX_BLOCK))
			.setProduct(ForestryFluids.WAX.getFluid(FluidType.BUCKET_VOLUME))
			.setMeltingPoint(500) //Arbitrary value, yes. Longer to warm up, but more efficient use of material
			.build(consumer, id("fabricator", "smelting", "wax_block"));
		new FabricatorSmeltingRecipeBuilder()
			.setResource(Ingredient.of(CoreItems.BEESWAX))
			.setProduct(ForestryFluids.WAX.getFluid(FluidType.BUCKET_VOLUME / 10)) //A /9 is easier, but messier.
			.setMeltingPoint(200) //Arbitrary value, yes. Shorter to warm up, but uses 10% more material
			.build(consumer, id("fabricator", "smelting", "wax"));
	}

	private static void registerFermenter(RecipeOutput consumer) {
		// Apiculture
		new FermenterRecipeBuilder()
			.setResource(Ingredient.of(CoreItems.HONEYDEW))
			.setFermentationValue(500)
			.setOutput(ForestryFluids.SHORT_MEAD.getFluid())
			.setFluidResource(ForestryFluids.HONEY.getFluid(1))
			.build(consumer, id("fermenter", "honeydew"));
		// Arboriculture
		addFermenterRecipes(consumer, "sapling", Ingredient.of(ItemTags.SAPLINGS), 250, ForestryFluids.BIOMASS);
		// Factory
		addFermenterRecipes(consumer, "cactus", Ingredient.of(Items.CACTUS), 50, ForestryFluids.BIOMASS);
		addFermenterRecipes(consumer, "wheat", Ingredient.of(Tags.Items.CROPS_WHEAT), 50, ForestryFluids.BIOMASS);
		addFermenterRecipes(consumer, "potato", Ingredient.of(Tags.Items.CROPS_POTATO), 100, ForestryFluids.BIOMASS);
		addFermenterRecipes(consumer, "sugar_cane", Ingredient.of(Items.SUGAR_CANE), 50, ForestryFluids.BIOMASS);
		addFermenterRecipes(consumer, "mushroom", Ingredient.of(Tags.Items.MUSHROOMS), 50, ForestryFluids.BIOMASS);
	}

	private static void addFermenterRecipes(RecipeOutput writer, String name, Ingredient resource, int fermentationValue, ForestryFluids output) {
		Fluid outputFluid = output.getFluid();

		new FermenterRecipeBuilder()
			.setResource(resource)
			.setFermentationValue(fermentationValue)
			.setFluidResource(new FluidStack(Fluids.WATER, 1))
			.setOutput(outputFluid)
			.build(writer, id("fermenter", name));
		new FermenterRecipeBuilder()
			.setResource(resource)
			.setFermentationValue(fermentationValue)
			.setFluidResource(ForestryFluids.JUICE.getFluid(1))
			.setOutput(outputFluid)
			.setModifier(1.5f)
			.build(writer, id("fermenter", name + "_juice"));
		new FermenterRecipeBuilder()
			.setResource(resource)
			.setFermentationValue(fermentationValue)
			.setFluidResource(ForestryFluids.HONEY.getFluid(1))
			.setOutput(outputFluid)
			.setModifier(1.5f)
			.build(writer, id("fermenter", name + "_honey"));
	}

	private static void registerHygroregulator(RecipeOutput consumer) {
		new HygroregulatorRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1))
			.setTemperatureSteps(-1)
			.setHumiditySteps(1)
			.build(consumer, id("hygroregulator", "water"));
		new HygroregulatorRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.LAVA, 1))
			.setTemperatureSteps(1)
			.setHumiditySteps(-1)
			.build(consumer, id("hygroregulator", "lava"));
		new HygroregulatorRecipeBuilder()
			.setLiquid(ForestryFluids.ICE.getFluid(1))
			.setRetainTime(10)
			.setTemperatureSteps(-2)
			.setHumiditySteps(2)
			.build(consumer, id("hygroregulator", "ice"));
	}

	private static void registerMoistener(RecipeOutput consumer) {
		new MoistenerRecipeBuilder()
			.setResource(Ingredient.of(Items.WHEAT_SEEDS))
			.setProduct(new ItemStack(Items.MYCELIUM))
			.setTimePerItem(5000)
			.build(consumer, id("moistener", "mycelium"));
		new MoistenerRecipeBuilder()
			.setResource(Ingredient.of(Items.COBBLESTONE))
			.setProduct(new ItemStack(Items.MOSSY_COBBLESTONE))
			.setTimePerItem(20000)
			.build(consumer, id("moistener", "mossy_cobblestone"));
		new MoistenerRecipeBuilder()
			.setResource(Ingredient.of(Items.STONE_BRICKS))
			.setProduct(new ItemStack(Items.MOSSY_STONE_BRICKS))
			.setTimePerItem(20000)
			.build(consumer, id("moistener", "mossy_stone_bricks"));
		new MoistenerRecipeBuilder()
			.setResource(Ingredient.of(Items.SPRUCE_LEAVES))
			.setProduct(new ItemStack(Items.PODZOL))
			.setTimePerItem(5000)
			.build(consumer, id("moistener", "podzol"));
	}

	private static void registerSqueezerContainer(RecipeOutput consumer) {
		new SqueezerContainerRecipeBuilder()
			.setProcessingTime(10)
			.setEmptyContainer(FluidsItems.CONTAINERS.stack(FluidContainerType.CAN))
			.setRemnants(CoreItems.TIN_INGOT.stack())
			.setRemnantsChance(0.05f)
			.build(consumer, id("squeezer", "container", "can"));
		new SqueezerContainerRecipeBuilder()
			.setProcessingTime(10)
			.setEmptyContainer(FluidsItems.CONTAINERS.stack(FluidContainerType.WAX_CAPSULE))
			.setRemnants(CoreItems.BEESWAX.stack())
			.setRemnantsChance(0.10f)
			.build(consumer, id("squeezer", "container", "capsule"));
		new SqueezerContainerRecipeBuilder()
			.setProcessingTime(10)
			.setEmptyContainer(FluidsItems.CONTAINERS.stack(FluidContainerType.REFRACTORY_CAPSULE))
			.setRemnants(CoreItems.REFRACTORY_WAX.stack())
			.setRemnantsChance(0.10f)
			.build(consumer, id("squeezer", "container", "refractory"));
	}

	private static void registerSqueezer(RecipeOutput consumer) {
		FluidStack honeyDropFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP);
		FluidStack honeyBlockFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP * 8);

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(CoreItems.HONEY_DROP)))
			.setFluidOutput(honeyDropFluid)
			.setRemnants(ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1))
			.setRemnantsChance(0.05f)
			.build(consumer, id("squeezer", "honey_drop"));
		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(CoreItems.HONEYDEW)))
			.setFluidOutput(honeyDropFluid)
			.build(consumer, id("squeezer", "honey_dew"));
		// reevaluate later
		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ApicultureItems.MAGMATIC_DROP)))
			.setFluidOutput(honeyDropFluid)
			.build(consumer, id("squeezer", "magmatic_drop"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ApicultureItems.BEE_COMBS.stack(EnumHoneyComb.SPONGY))))
			.setFluidOutput(honeyDropFluid)
			.setRemnants(new ItemStack(Items.SPONGE))
			.setRemnantsChance(2 / 100f)
			.build(consumer, id("squeezer", "sponge_comb"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(60)
			.setResources(NonNullList.withSize(1, Ingredient.of(Items.HONEY_BLOCK)))
			.setFluidOutput(honeyBlockFluid)
			.build(consumer, id("squeezer", "honey_block"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ApicultureItems.EXPERIENCE_DROP)))
			.setFluidOutput(ForestryFluids.EXPERIENCE.getFluid(250))
			.build(consumer, id("squeezer", "experience_drop"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(20)
			.setResources(Util.make(NonNullList.create(), (ingredients) -> {
				ingredients.add(Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHOR)));
				ingredients.add(Ingredient.of(Items.SAND, Items.RED_SAND));
			}))
			.setFluidOutput(new FluidStack(Fluids.LAVA, 500))
			.build(consumer, id("squeezer", "lava_sand"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(30)
			.setResources(Util.make(NonNullList.create(), (ingredients) -> {
				ingredients.add(Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHOR)));
				ingredients.add(Ingredient.of(Items.COBBLESTONE));
			}))
			.setFluidOutput(new FluidStack(Fluids.LAVA, 500))
			.build(consumer, id("squeezer", "lava"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(20)
			.setResources(Util.make(NonNullList.create(), (ingredients) -> {
				ingredients.add(Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHOR)));
				ingredients.add(Ingredient.of(Items.MAGMA_BLOCK));
			}))
			.setFluidOutput(new FluidStack(Fluids.LAVA, 1000))
			.build(consumer, id("squeezer", "lava_magma"));

		int seedOilAmount = Preference.SQUEEZED_LIQUID_SEED;

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(Tags.Items.SEEDS)))
			.setFluidOutput(ForestryFluids.SEED_OIL.getFluid(seedOilAmount))
			.build(consumer, id("squeezer", "seeds"));

		float mulchMultiplier = Preference.SQUEEZED_MULCH_APPLE;
		int juiceMultiplier = Preference.SQUEEZED_LIQUID_APPLE;

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(Items.APPLE, Items.CARROT)))
			.setFluidOutput(ForestryFluids.JUICE.getFluid(juiceMultiplier))
			.setRemnants(CoreItems.MULCH.stack())
			.setRemnantsChance(mulchMultiplier)
			.build(consumer, id("squeezer", "mulch"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(Items.CACTUS)))
			.setFluidOutput(new FluidStack(Fluids.WATER, 500))
			.build(consumer, id("squeezer", "cactus"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(List.of(
				Ingredient.of(Items.SNOWBALL),
				Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)),
				Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)),
				Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)),
				Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD))
			))
			.setFluidOutput(ForestryFluids.ICE.getFluid(4000))
			.build(consumer, id("squeezer", "ice"));

		int seedOilMultiplier = Preference.SQUEEZED_LIQUID_SEED;

		ItemStack mulch = new ItemStack(CoreItems.MULCH);
		Fluid seedOil = ForestryFluids.SEED_OIL.getFluid();
		Fluid juice = ForestryFluids.JUICE.getFluid();

		new SqueezerRecipeBuilder()
			.setProcessingTime(20)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.CHERRY)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier / 4))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier / 4)
			.build(consumer, id("squeezer", "fruit", "cherry"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(60)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.WALNUT)))
			.setFluidOutput(new FluidStack(seedOil, seedOilMultiplier * 5))
			.setRemnants(mulch)
			.setRemnantsChance(0.05F)
			.build(consumer, id("squeezer", "fruit", "walnut"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(70)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.CHESTNUT)))
			.setFluidOutput(new FluidStack(seedOil, seedOilMultiplier * 8))
			.setRemnants(mulch)
			.setRemnantsChance(0.02F)
			.build(consumer, id("squeezer", "fruit", "chestnut"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.LEMON)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier * 2))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier / 2f)
			.build(consumer, id("squeezer", "fruit", "lemon"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.PLUM)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier / 2))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier * 3f)
			.build(consumer, id("squeezer", "fruit", "plum"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.PAPAYA)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier * 3))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier / 2f)
			.build(consumer, id("squeezer", "fruit", "papaya"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.DATE)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier / 4))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier)
			.build(consumer, id("squeezer", "fruit", "dates"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.COCONUT)))
			.setFluidOutput(new FluidStack(NeoForgeMod.MILK.get(), 500))
			.setRemnants(mulch)
			.setRemnantsChance(0.25f)
			.build(consumer, id("squeezer", "fruit", "coconut"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.FEIJOA)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier / 2))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier)
			.build(consumer, id("squeezer", "fruit", "feijoa"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.ORANGE)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier * 2))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier / 2f)
			.build(consumer, id("squeezer", "fruit", "orange"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(70)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.OLIVE)))
			.setFluidOutput(new FluidStack(seedOil, seedOilMultiplier * 10))
			.setRemnants(mulch)
			.setRemnantsChance(0.02F)
			.build(consumer, id("squeezer", "fruit", "olive"));

		new SqueezerRecipeBuilder()
			.setProcessingTime(10)
			.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.PEAR)))
			.setFluidOutput(new FluidStack(juice, juiceMultiplier / 2))
			.setRemnants(mulch)
			.setRemnantsChance(mulchMultiplier * 3f)
			.build(consumer, id("squeezer", "fruit", "pear"));
	}

	// Deviation from 1.20.1: NeoForge dropped ConditionalRecipe, so a recipe that only exists when some
	// tag is filled is written straight to consumer.withConditions(...) instead of being wrapped
	private static void registerSmelter(RecipeOutput consumer) {
		new SmelterRecipeBuilder()
			.addIngredient(Ingredient.of(Tags.Items.INGOTS_COPPER), 3)
			.addIngredient(Ingredient.of(ForestryTags.Items.INGOTS_TIN))
			.setOutput(Ingredient.of(ForestryTags.Items.INGOTS_BRONZE), 4)
			.setProcessingTime(40)
			.build(consumer, id("smelter", "bronze_from_ingots"));

		new SmelterRecipeBuilder()
			.addIngredient(Ingredient.of(Tags.Items.RAW_MATERIALS_COPPER), 3)
			.addIngredient(Ingredient.of(ForestryTags.Items.RAW_MATERIALS_TIN))
			.setOutput(Ingredient.of(ForestryTags.Items.INGOTS_BRONZE), 4)
			.setProcessingTime(40)
			.build(consumer, id("smelter", "bronze_from_raw_materials"));

		// Silicon comes from coke where a mod supplies it and from plain coal where none does
		new SmelterRecipeBuilder()
			.addIngredient(Ingredient.of(Tags.Items.GEMS_QUARTZ), 3)
			.addIngredient(Ingredient.of(Items.COAL), 2)
			.setOutput(Ingredient.of(ForestryTags.Items.SILICON), 3)
			.setProcessingTime(1200)
			.build(consumer.withConditions(new TagEmptyCondition(ForestryTags.Items.COAL_COKE)), id("smelter", "silicon_from_coal"));

		new SmelterRecipeBuilder()
			.addIngredient(Ingredient.of(Tags.Items.GEMS_QUARTZ), 3)
			.addIngredient(Ingredient.of(ForestryTags.Items.COAL_COKE), 1)
			.setOutput(Ingredient.of(ForestryTags.Items.SILICON), 3)
			.setProcessingTime(1200)
			.build(consumer.withConditions(not(new TagEmptyCondition(ForestryTags.Items.COAL_COKE))), id("smelter", "silicon_from_coke"));


		// Alloys forestry does not add itself. Each pair loads only when another mod supplies both the
		// component and the alloy
		alloy(consumer, "invar", Tags.Items.INGOTS_IRON, 2, ForestryTags.Items.INGOTS_NICKEL, ForestryTags.Items.INGOTS_INVAR, 3, false);
		alloy(consumer, "invar", Tags.Items.RAW_MATERIALS_IRON, 2, ForestryTags.Items.RAW_MATERIALS_NICKEL, ForestryTags.Items.INGOTS_INVAR, 3, true);

		alloy(consumer, "brass", Tags.Items.INGOTS_COPPER, 1, ForestryTags.Items.INGOTS_ZINC, ForestryTags.Items.INGOTS_BRASS, 2, false);
		alloy(consumer, "brass", Tags.Items.RAW_MATERIALS_COPPER, 1, ForestryTags.Items.RAW_MATERIALS_ZINC, ForestryTags.Items.INGOTS_BRASS, 2, true);

		alloy(consumer, "electrum", Tags.Items.INGOTS_GOLD, 1, ForestryTags.Items.INGOTS_SILVER, ForestryTags.Items.INGOTS_ELECTRUM, 2, false);
		alloy(consumer, "electrum", Tags.Items.RAW_MATERIALS_GOLD, 1, ForestryTags.Items.RAW_MATERIALS_SILVER, ForestryTags.Items.INGOTS_ELECTRUM, 2, true);

		alloy(consumer, "constantan", Tags.Items.INGOTS_COPPER, 1, ForestryTags.Items.INGOTS_NICKEL, ForestryTags.Items.INGOTS_CONSTANTAN, 2, false);
		alloy(consumer, "constantan", Tags.Items.RAW_MATERIALS_COPPER, 1, ForestryTags.Items.RAW_MATERIALS_NICKEL, ForestryTags.Items.INGOTS_CONSTANTAN, 2, true);
	}

	/**
	 * Writes one modded alloy recipe, guarded on both the alloying component and the alloy itself
	 * being present. Deviation from 1.20.1: the eight alloy recipes were spelled out one by one there
	 *
	 * @param name      The first path segment of the recipe id. Ex. "invar" -> "smelter/invar_from_ingots"
	 * @param base      The tag of the metal the alloy is mostly made of
	 * @param baseCount The number of base items one recipe consumes
	 * @param component The tag of the metal alloyed into the base
	 * @param alloy     The tag of the resulting alloy
	 * @param count     The number of alloy items one recipe produces
	 * @param raw       Whether this is the raw material variant of the recipe
	 */
	private static void alloy(RecipeOutput consumer, String name, TagKey<Item> base, int baseCount, TagKey<Item> component, TagKey<Item> alloy, int count, boolean raw) {
		new SmelterRecipeBuilder()
			.addIngredient(Ingredient.of(base), baseCount)
			.addIngredient(Ingredient.of(component))
			.setOutput(Ingredient.of(alloy), count)
			.setProcessingTime(40)
			.build(consumer.withConditions(not(new TagEmptyCondition(component)), not(new TagEmptyCondition(alloy))),
				id("smelter", name + (raw ? "_from_raw_materials" : "_from_ingots")));
	}

	private static ICondition not(ICondition condition) {
		return new NotCondition(condition);
	}

	private static void registerStill(RecipeOutput consumer) {
		FluidStack biomass = ForestryFluids.BIOMASS.getFluid(STILL_DESTILLATION_INPUT);
		FluidStack ethanol = ForestryFluids.BIO_ETHANOL.getFluid(STILL_DESTILLATION_OUTPUT);

		new StillRecipeBuilder()
			.setTimePerUnit(STILL_DESTILLATION_DURATION)
			.setInput(biomass)
			.setOutput(ethanol)
			.build(consumer, id("still", "ethanol"));
	}
}
