package forestry.core.data;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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

import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import forestry.api.ForestryTags;
import forestry.api.arboriculture.IWoodAccess;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.TreeManager;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.circuits.ICircuit;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.blocks.BlockTypeApiculture;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.VanillaWoodType;
import forestry.arboriculture.WoodAccess;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.blocks.EnumResourceType;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.circuits.ItemCircuitBoard;
import forestry.core.config.Constants;
import forestry.core.config.Preference;
import forestry.core.data.builder.CarpenterRecipeBuilder;
import forestry.core.data.builder.CentrifugeRecipeBuilder;
import forestry.core.data.builder.FabricatorRecipeBuilder;
import forestry.core.data.builder.FabricatorSmeltingRecipeBuilder;
import forestry.core.data.builder.FermenterRecipeBuilder;
import forestry.core.data.builder.HygroregulatorRecipeBuilder;
import forestry.core.data.builder.MoistenerRecipeBuilder;
import forestry.core.data.builder.SqueezerContainerRecipeBuilder;
import forestry.core.data.builder.SqueezerRecipeBuilder;
import forestry.core.data.builder.StillRecipeBuilder;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.fluids.ForestryFluids;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.core.utils.ModUtil;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.features.CultivationBlocks;
import forestry.energy.blocks.EngineBlockType;
import forestry.energy.features.EnergyBlocks;
import forestry.factory.blocks.BlockTypeFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.features.FarmingBlocks;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.lepidopterology.features.LepidopterologyRecipes;
import forestry.mail.blocks.BlockTypeMail;
import forestry.mail.features.MailBlocks;
import forestry.mail.features.MailItems;
import forestry.mail.items.EnumStampDefinition;
import forestry.mail.items.ItemLetter;
import forestry.modules.features.FeatureItem;
import forestry.sorting.features.SortingBlocks;
import forestry.storage.features.BackpackItems;
import forestry.storage.features.CrateItems;
import forestry.storage.items.ItemCrated;
import forestry.worktable.features.WorktableBlocks;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import thedarkcolour.modkit.data.MKRecipeProvider;
import static thedarkcolour.modkit.data.MKRecipeProvider.ingredient;
import static thedarkcolour.modkit.data.MKRecipeProvider.path;

// todo split into smaller classes so that my computer doesn't die
public class ForestryRecipeProvider {
	public static final int STILL_DESTILLATION_DURATION = 100;
	public static final int STILL_DESTILLATION_INPUT = 10;
	public static final int STILL_DESTILLATION_OUTPUT = 3;

	public static ItemStack getContainer(EnumContainerType type, ForestryFluids fluid) {
		return getContainer(type, fluid.getFluid());
	}

	public static ItemStack getContainer(EnumContainerType type, Fluid fluid) {
		ItemStack container = FluidsItems.CONTAINERS.stack(type);
		LazyOptional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(container);
		return fluidHandlerCap.map(handler -> {
			handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
			return container;
		}).orElse(ItemStack.EMPTY);
	}

	public static void addRecipes(Consumer<FinishedRecipe> consumer, MKRecipeProvider recipes) {
		// Vanilla recipe types
		registerArboricultureRecipes(recipes);
		registerApicultureRecipes(recipes);
		registerFoodRecipes(recipes);
		registerBackpackRecipes(recipes);
		registerCharcoalRecipes(recipes);
		registerCoreRecipes(recipes);
		registerCultivationRecipes(recipes);
		registerFactoryRecipes(recipes);
		registerFarmingRecipes(recipes);
		registerFluidsRecipes(recipes);
		registerLepidopterologyRecipes(recipes);
		registerMailRecipes(recipes);
		registerSortingRecipes(recipes);
		registerWorktableRecipes(recipes);
		registerEnergyRecipes(recipes);

		// Forestry recipe types
		registerCarpenter(consumer);
		registerCentrifuge(consumer);
		registerFabricator(consumer);
		registerFabricatorSmelting(consumer);
		registerFermenter(consumer);
		registerHygroregulator(consumer);
		registerMoistener(consumer);
		registerSqueezerContainer(consumer);
		registerSqueezer(consumer);
		registerStill(consumer);
	}

	private static void registerApicultureRecipes(MKRecipeProvider recipes) {
		registerCombRecipes(recipes);

		BlockAlveary plain = ApicultureBlocks.ALVEARY.get(BlockAlvearyType.PLAIN).block();
		ItemLike goldElectronTube = CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD);

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, plain, recipe -> {
			recipe.define('X', CoreItems.IMPREGNATED_CASING);
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SCENTED_PANELING));
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlvearyType.FAN).block(), recipe -> {
			recipe.define('#', goldElectronTube);
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("I I");
			recipe.pattern(" X ");
			recipe.pattern("I#I");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlvearyType.HEATER).block(), recipe -> {
			recipe.define('#', goldElectronTube);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.define('X', plain);
			recipe.define('S', Tags.Items.STONE);
			recipe.pattern("#I#");
			recipe.pattern(" X ");
			recipe.pattern("SSS");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlvearyType.HYGRO).block(), recipe -> {
			recipe.define('G', Tags.Items.GLASS);
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("GIG");
			recipe.pattern("GXG");
			recipe.pattern("GIG");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlvearyType.SIEVE).block(), recipe -> {
			recipe.define('W', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK));
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("III");
			recipe.pattern(" X ");
			recipe.pattern("WWW");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlvearyType.STABILISER).block(), recipe -> {
			recipe.define('X', plain);
			recipe.define('G', Tags.Items.GEMS_QUARTZ);
			recipe.pattern("G G");
			recipe.pattern("GXG");
			recipe.pattern("G G");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlvearyType.SWARMER).block(), recipe -> {
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

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureBlocks.BASE.get(BlockTypeApiculture.APIARY).block(), recipe -> {
			recipe.define('S', ItemTags.WOODEN_SLABS);
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('C', CoreItems.IMPREGNATED_CASING);
			recipe.pattern("SSS");
			recipe.pattern("PCP");
			recipe.pattern("PPP");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureBlocks.BASE.get(BlockTypeApiculture.BEE_HOUSE).block(), recipe -> {
			recipe.define('S', ItemTags.WOODEN_SLABS);
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('C', ForestryTags.Items.BEE_COMBS);
			recipe.pattern("SSS");
			recipe.pattern("PCP");
			recipe.pattern("PPP");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST), recipe -> {
			recipe.define('G', Tags.Items.GLASS);
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
			recipe.define('#', CoreItems.STICK_IMPREGNATED);
			recipe.define('S', Tags.Items.STRING);
			recipe.pattern("###");
			recipe.pattern("#S#");
			recipe.pattern("###");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureItems.FRAME_UNTREATED, recipe -> {
			recipe.define('#', Tags.Items.RODS_WOODEN);
			recipe.define('S', Tags.Items.STRING);
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

		recipes.shapedCrafting(RecipeCategory.TOOLS, ApicultureItems.SCOOP, recipe -> {
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
			recipe.define('L', Tags.Items.LEATHER);
			recipe.pattern("LS#");
			recipe.pattern("LF#");
			recipe.pattern("###");
		});

		recipes.shapedCrafting("glistering_melon_slice", RecipeCategory.MISC, Items.GLISTERING_MELON_SLICE, recipe -> {
			recipe.define('#', ApicultureItems.HONEY_DROP);
			recipe.define('X', ApicultureItems.HONEYDEW);
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

		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureItems.WAX_CAST, recipe -> {
			recipe.define('#', beesWax);
			recipe.pattern("###");
			recipe.pattern("# #");
			recipe.pattern("###");
		});
	}

	private static void registerCombRecipes(MKRecipeProvider recipes) {
		for (EnumHoneyComb honeyComb : EnumHoneyComb.VALUES) {
			ItemLike comb = ApicultureItems.BEE_COMBS.get(honeyComb);
			Block combBlock = ApicultureBlocks.BEE_COMB.get(honeyComb).block();
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
			recipe.define('#', Tags.Items.GLASS);
			recipe.pattern(" # ");
			recipe.pattern("XYX");
			recipe.pattern("XXX");
		});
	}

	private static void registerWoodRecipes(MKRecipeProvider recipes) {
		IWoodAccess woodAccess = WoodAccess.INSTANCE;
		List<IWoodType> woodTypes = woodAccess.getRegisteredWoodTypes();

		for (IWoodType woodType : woodTypes) {
			Block planks = woodAccess.getBlock(woodType, WoodBlockKind.PLANKS, false).getBlock();
			Block fireproofPlanks = woodAccess.getBlock(woodType, WoodBlockKind.PLANKS, true).getBlock();
			Block log = woodAccess.getBlock(woodType, WoodBlockKind.LOG, false).getBlock();
			Block wood = woodAccess.getBlock(woodType, WoodBlockKind.WOOD, false).getBlock();
			Block fireproofLog = woodAccess.getBlock(woodType, WoodBlockKind.LOG, true).getBlock();
			Block door = woodAccess.getBlock(woodType, WoodBlockKind.DOOR, false).getBlock();
			Block fence = woodAccess.getBlock(woodType, WoodBlockKind.FENCE, false).getBlock();
			Block fireproofFence = woodAccess.getBlock(woodType, WoodBlockKind.FENCE, true).getBlock();
			Block fenceGate = woodAccess.getBlock(woodType, WoodBlockKind.FENCE_GATE, false).getBlock();
			Block fireproofFenceGate = woodAccess.getBlock(woodType, WoodBlockKind.FENCE_GATE, true).getBlock();
			Block slab = woodAccess.getBlock(woodType, WoodBlockKind.SLAB, false).getBlock();
			Block fireproofSlab = woodAccess.getBlock(woodType, WoodBlockKind.SLAB, true).getBlock();
			Block stairs = woodAccess.getBlock(woodType, WoodBlockKind.STAIRS, false).getBlock();
			Block fireproofStairs = woodAccess.getBlock(woodType, WoodBlockKind.STAIRS, true).getBlock();

			recipes.woodenDoor(door, Ingredient.of(planks, fireproofPlanks));

			// Regular (Forestry)
			if (woodType instanceof ForestryWoodType) {
				makeCommonWoodenSet(recipes, planks, log, fence, fenceGate, slab, stairs);
				recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, wood, 3, Ingredient.of(log), "bark");
			}

			// Fireproof (Vanilla & Forestry)
			makeCommonWoodenSet(recipes, fireproofPlanks, fireproofLog, fireproofFence, fireproofFenceGate, fireproofSlab, fireproofStairs);
		}
	}

	// Shared between regular and fireproof recipes
	private static void makeCommonWoodenSet(MKRecipeProvider recipes, Block planks, Block log, Block fence, Block fenceGate, Block slab, Block stairs) {
		recipes.shapelessCrafting(RecipeCategory.BUILDING_BLOCKS, planks, 4, "planks", log);
		recipes.woodenFence(fence, planks);
		recipes.woodenFenceGate(fenceGate, planks);
		recipes.woodenSlab(slab, planks);
		recipes.woodenStairs(stairs, planks);
	}

	private static void registerFoodRecipes(MKRecipeProvider recipes) {
		ItemLike waxCapsule = FluidsItems.CONTAINERS.get(EnumContainerType.CAPSULE);
		ItemLike honeyDrop = ApicultureItems.HONEY_DROP;

		recipes.shapedCrafting(RecipeCategory.FOOD, ApicultureItems.AMBROSIA, recipe -> {
			recipe.define('#', ApicultureItems.HONEYDEW);
			recipe.define('X', ApicultureItems.ROYAL_JELLY);
			recipe.define('Y', waxCapsule);
			recipe.pattern("#Y#");
			recipe.pattern("XXX");
			recipe.pattern("###");
		});

		recipes.shapedCrafting(RecipeCategory.FOOD, ApicultureItems.HONEY_POT, recipe -> {
			recipe.define('#', honeyDrop);
			recipe.define('X', waxCapsule);
			recipe.pattern("# #");
			recipe.pattern(" X ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting(RecipeCategory.FOOD, ApicultureItems.HONEYED_SLICE, recipe -> {
			recipe.define('#', honeyDrop);
			recipe.define('X', Items.BREAD);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});
	}

	private static void registerBackpackRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.ADVENTURER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.BONES);
			recipe.define('X', Tags.Items.STRING);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.BUILDER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Items.CLAY_BALL);
			recipe.define('X', Tags.Items.STRING);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.DIGGER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.STONE);
			recipe.define('X', Tags.Items.STRING);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.FORESTER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', ItemTags.LOGS);
			recipe.define('X', Tags.Items.STRING);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.HUNTER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.FEATHERS);
			recipe.define('X', Tags.Items.STRING);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("VYV");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.MINER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.INGOTS_IRON);
			recipe.define('X', Tags.Items.STRING);
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
			recipe.define('X', Tags.Items.STRING);
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

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.WOOD_PILE.block(), recipe -> {
			recipe.define('L', ItemTags.LOGS);
			recipe.pattern(" L ");
			recipe.pattern("L L");
			recipe.pattern(" L ");
		});

		recipes.shapelessCrafting(RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.WOOD_PILE_DECORATIVE.block(), 1, CharcoalBlocks.WOOD_PILE.block());

		recipes.shapelessCrafting("wood_pile_from_decorative", RecipeCategory.BUILDING_BLOCKS, CharcoalBlocks.WOOD_PILE.block(), 1, CharcoalBlocks.WOOD_PILE_DECORATIVE.block());
	}

	private static void registerCoreRecipes(MKRecipeProvider recipes) {
		recipes.oreSmelting(ingredient(CoreBlocks.APATITE_ORE.get(), CoreBlocks.DEEPSLATE_APATITE_ORE.get()), CoreItems.APATITE, 0.5f, 200);
		recipes.oreSmelting(ingredient(CoreBlocks.TIN_ORE.get(), CoreBlocks.DEEPSLATE_TIN_ORE.get(), CoreItems.RAW_TIN), CoreItems.INGOT_TIN, 0.5f, 200);
		recipes.smelting(Ingredient.of(CoreItems.PEAT.item()), CoreItems.ASH, 0.0f, 200);
		recipes.storage3x3(CoreBlocks.RAW_TIN_BLOCK, CoreItems.RAW_TIN);

		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER), recipe -> {
			recipe.define('T', CoreItems.PORTABLE_ALYZER);
			recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("XTX");
			recipe.pattern(" Y ");
			recipe.pattern("X X");
		});
		recipes.storage3x3(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.APATITE), CoreItems.APATITE);
		recipes.storage3x3(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.BRONZE), CoreItems.INGOT_BRONZE);
		recipes.storage3x3(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.TIN), CoreItems.INGOT_TIN);
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.BRONZE_PICKAXE, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern("###");
			recipe.pattern(" X ");
			recipe.pattern(" X ");
		});
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.BRONZE_SHOVEL, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('X', Tags.Items.RODS_WOODEN);
			recipe.pattern(" # ");
			recipe.pattern(" X ");
			recipe.pattern(" X ");
		});

		gear(recipes, CoreItems.GEAR_BRONZE, ForestryTags.Items.INGOTS_BRONZE);
		gear(recipes, CoreItems.GEAR_TIN, ForestryTags.Items.INGOTS_TIN);
		gear(recipes, CoreItems.GEAR_COPPER, Tags.Items.INGOTS_COPPER);

		recipes.shapelessCrafting("ingot_bronze_alloying", RecipeCategory.MISC, CoreItems.INGOT_BRONZE, 4, ForestryTags.Items.INGOTS_TIN, ObjectIntPair.of(Items.COPPER_INGOT, 3));
		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.KIT_PICKAXE, 1, CoreItems.BRONZE_PICKAXE, CoreItems.CARTON);
		recipes.shapelessCrafting(RecipeCategory.TOOLS, CoreItems.KIT_SHOVEL, 1, CoreItems.BRONZE_SHOVEL, CoreItems.CARTON);
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
		recipes.shapedCrafting(RecipeCategory.TOOLS, CoreItems.PORTABLE_ALYZER, recipe -> {
			recipe.define('#', Tags.Items.GLASS_PANES);
			recipe.define('X', ForestryTags.Items.INGOTS_TIN);
			recipe.define('R', Tags.Items.DUSTS_REDSTONE);
			recipe.define('D', Tags.Items.GEMS_DIAMOND);
			recipe.pattern("X#X");
			recipe.pattern("X#X");
			recipe.pattern("RDR");
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
		bogRecipe(recipes, 8, getContainer(EnumContainerType.CAN, Fluids.WATER), "can");
		bogRecipe(recipes, 8, getContainer(EnumContainerType.CAPSULE, Fluids.WATER), "wax_capsule");
		bogRecipe(recipes, 8, getContainer(EnumContainerType.REFRACTORY, Fluids.WATER), "refractory");
		bogRecipe(recipes, 6, new ItemStack(Items.WATER_BUCKET), "bucket");

		recipes.shapedCrafting("can", RecipeCategory.MISC, FluidsItems.CONTAINERS.get(EnumContainerType.CAN), 12, recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_TIN);
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting("capsule", RecipeCategory.MISC, FluidsItems.CONTAINERS.get(EnumContainerType.CAPSULE), 4, recipe -> {
			recipe.define('#', CoreItems.BEESWAX);
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		recipes.shapedCrafting("refractory_capsule", RecipeCategory.MISC, FluidsItems.CONTAINERS.get(EnumContainerType.REFRACTORY), 4, recipe -> {
			recipe.define('#', CoreItems.REFRACTORY_WAX);
			recipe.pattern(" # ");
			recipe.pattern("# #");
		});

		// Books
		recipes.shapelessCrafting("foresters_manual_honeydrop", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, ApicultureItems.HONEY_DROP);
		recipes.shapelessCrafting("foresters_manual_sapling", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, ItemTags.SAPLINGS);
		recipes.shapelessCrafting("foresters_manual_butterfly", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, LepidopterologyItems.BUTTERFLY_GE);
	}

	private static void bogRecipe(MKRecipeProvider recipes, int amount, ItemStack container, String name) {
		recipes.shapedCrafting("bog_earth_" + name, RecipeCategory.BUILDING_BLOCKS, CoreBlocks.BOG_EARTH, amount, recipe -> {
			recipe.define('#', Blocks.DIRT);
			recipe.define('X', StrictNBTIngredient.of(container));
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

	private static EnumElectronTube getElectronTube(BlockTypePlanter planter) {
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

	private static void registerCultivationRecipes(MKRecipeProvider recipes) {
		for (BlockTypePlanter planter : BlockTypePlanter.VALUES) {
			Block managed = CultivationBlocks.MANAGED_PLANTER.get(planter).block();
			Block manual = CultivationBlocks.MANUAL_PLANTER.get(planter).block();

			recipes.shapedCrafting(RecipeCategory.MISC, managed, recipe -> {
				recipe.define('G', Tags.Items.GLASS);
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

	private static void registerFactoryRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.BOTTLER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', FluidsItems.CONTAINERS.get(EnumContainerType.CAN));
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CENTRIFUGE).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', Items.COPPER_INGOT);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', Tags.Items.INGOTS_GOLD);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.define('Z', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("XZX");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.FERMENTER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.MOISTENER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', ForestryTags.Items.GEARS_COPPER);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.RAINMAKER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', ForestryTags.Items.GEARS_TIN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.RAINTANK).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', Tags.Items.INGOTS_IRON);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.SQUEEZER).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', ForestryTags.Items.INGOTS_TIN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("XYX");
			recipe.pattern("X#X");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, FactoryBlocks.TESR.get(BlockTypeFactoryTesr.STILL).block(), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', Tags.Items.DUSTS_REDSTONE);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern("X#X");
			recipe.pattern("#Y#");
			recipe.pattern("X#X");
		});
	}

	private static void registerFarmingRecipes(MKRecipeProvider recipes) {
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
				recipe.define('X', Tags.Items.GLASS);
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

	private static void registerFluidsRecipes(MKRecipeProvider recipes) {
		for (EnumContainerType containerType : EnumContainerType.values()) {
			recipes.shapedCrafting("cake_" + containerType.getSerializedName(), RecipeCategory.FOOD, Items.CAKE, recipe -> {
				recipe.define('A', StrictNBTIngredient.of(getContainer(containerType, ForgeMod.MILK.get())));
				recipe.define('B', Items.SUGAR);
				recipe.define('C', Items.WHEAT);
				recipe.define('E', Items.EGG);
				recipe.pattern("AAA");
				recipe.pattern("BEB");
				recipe.pattern("CCC");
			});
		}
	}

	private static void registerLepidopterologyRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST), recipe -> {
			recipe.define('#', Tags.Items.GLASS);
			recipe.define('X', LepidopterologyItems.BUTTERFLY_GE);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern(" # ");
			recipe.pattern("XYX");
			recipe.pattern("XXX");
		});
		recipes.special("butterfly_mating", LepidopterologyRecipes.MATING_SERIALIZER);
	}

	private static void registerMailRecipes(MKRecipeProvider recipes) {
		recipes.shapelessCrafting(RecipeCategory.MISC, MailItems.CATALOGUE, 1, Items.BOOK, ForestryTags.Items.STAMPS);
		Ingredient sealant = Ingredient.merge(List.of(Ingredient.of(ForestryTags.Items.PROPOLIS), Ingredient.of(Tags.Items.SLIMEBALLS)));
		recipes.shapelessCrafting(RecipeCategory.MISC, MailItems.LETTERS.get(ItemLetter.Size.EMPTY, ItemLetter.State.FRESH), 1, Items.PAPER, sealant);

		recipes.shapedCrafting(RecipeCategory.MISC, MailBlocks.BASE.get(BlockTypeMail.MAILBOX).block(), recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_TIN);
			recipe.define('X', Tags.Items.CHESTS_WOODEN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern(" # ");
			recipe.pattern("#Y#");
			recipe.pattern("XXX");
		});

		Item[] emptiedLetter = MailItems.LETTERS.getRowFeatures(ItemLetter.Size.EMPTY).stream()
				.map(FeatureItem::item)
				.toArray(Item[]::new);
		recipes.shapedCrafting("paper_from_letters", RecipeCategory.MISC, Items.PAPER, recipe -> {
			recipe.define('#', Ingredient.of(emptiedLetter));
			recipe.pattern(" # ");
			recipe.pattern(" # ");
			recipe.pattern(" # ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, MailBlocks.BASE.get(BlockTypeMail.TRADE_STATION).block(), recipe -> {
			recipe.define('#', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BRONZE));
			recipe.define('X', Tags.Items.CHESTS_WOODEN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.define('Z', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.IRON));
			recipe.define('W', StrictNBTIngredient.of(ItemCircuitBoard.createCircuitboard(EnumCircuitBoardType.REFINED, null, new ICircuit[]{})));
			recipe.pattern("Z#Z");
			recipe.pattern("#Y#");
			recipe.pattern("XWX");
		});

		Ingredient glue = Ingredient.merge(List.of(
				Ingredient.of(ForestryTags.Items.DROP_HONEY),
				Ingredient.of(Items.SLIME_BALL)
		));

		for (EnumStampDefinition stampDefinition : EnumStampDefinition.VALUES) {
			recipes.shapedCrafting(RecipeCategory.MISC, MailItems.STAMPS.get(stampDefinition), 9, recipe -> {
				recipe.define('X', stampDefinition.getCraftingIngredient());
				recipe.define('#', Items.PAPER);
				recipe.define('Z', glue);
				recipe.pattern("XXX");
				recipe.pattern("###");
				recipe.pattern("ZZZ");
			});
		}
	}

	private static void registerSortingRecipes(MKRecipeProvider recipes) {
		Ingredient ing = Ingredient.merge(List.of(Ingredient.of(LepidopterologyItems.CATERPILLAR_GE, ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL)), Ingredient.of(ForestryTags.Items.FORESTRY_FRUITS)));

		recipes.shapedCrafting(RecipeCategory.MISC, SortingBlocks.FILTER.block(), 2, recipe -> {
			recipe.define('B', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('D', Tags.Items.GEMS_DIAMOND);
			recipe.define('F', ing);
			recipe.define('W', ItemTags.PLANKS);
			recipe.define('G', Tags.Items.GLASS);
			recipe.pattern("WDW");
			recipe.pattern("FGF");
			recipe.pattern("BDB");
		});
	}

	private static void registerWorktableRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, WorktableBlocks.WORKTABLE.block(), recipe -> {
			recipe.define('B', Items.BOOK);
			recipe.define('T', ForestryTags.Items.CRAFTING_TABLES);
			recipe.define('C', Tags.Items.CHESTS_WOODEN);
			recipe.pattern("B");
			recipe.pattern("T");
			recipe.pattern("C");
		});
	}

	private static void registerEnergyRecipes(MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK), recipe -> {
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('I', Tags.Items.GLASS);
			recipe.define('Q', ForestryTags.Items.GEARS_COPPER);
			recipe.define('D', Items.PISTON);
			recipe.define('C', Items.CLOCK);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDC");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS), recipe -> {
			recipe.define('P', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('I', Tags.Items.GLASS);
			recipe.define('Q', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.PEAT), recipe -> {
			recipe.define('P', Tags.Items.INGOTS_COPPER);
			recipe.define('I', Tags.Items.GLASS);
			recipe.define('Q', ForestryTags.Items.GEARS_COPPER);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});
	}

	private static void registerCarpenter(Consumer<FinishedRecipe> consumer) {
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
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.STICK_IMPREGNATED, 2)
						.pattern("#")
						.pattern("#")
						.define('#', ItemTags.LOGS))
				.build(consumer, id("carpenter", "impregnated_stick"));
		new CarpenterRecipeBuilder()
				.setLiquid(new FluidStack(Fluids.WATER, 250))
				.setBox(Ingredient.EMPTY)
				.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.WOOD_PULP, 4)
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
						.define('X', Tags.Items.SAND)
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
						.define('Y', FluidsItems.CONTAINERS.get(EnumContainerType.CAN))
						.define('Z', ApicultureItems.HONEY_DROP))
				.build(consumer, id("carpenter", "iodine_charge"));
		new CarpenterRecipeBuilder()
				.setLiquid(new FluidStack(Fluids.WATER, 1000))
				.setBox(Ingredient.EMPTY)
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.DISSIPATION_CHARGE))
						.pattern("Z#Z")
						.pattern("#Y#")
						.pattern("X#X")
						.define('#', ApicultureItems.ROYAL_JELLY)
						.define('X', Items.GUNPOWDER)
						.define('Y', FluidsItems.CONTAINERS.get(EnumContainerType.CAN))
						.define('Z', ApicultureItems.HONEYDEW))
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
						.pattern("XXX")
						.pattern("XXX")
						.pattern("XXX")
						.define('X', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP)))
				.build(consumer, id("carpenter", "woven_silk"));
		new CarpenterRecipeBuilder()
				.setBox(Ingredient.EMPTY)
				.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.INGOT_BRONZE, 2)
						.requires(CoreItems.BROKEN_BRONZE_PICKAXE))
				.build(consumer, id("carpenter", "reclaim_bronze_pickaxe"));
		new CarpenterRecipeBuilder()
				.setBox(Ingredient.EMPTY)
				.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.INGOT_BRONZE, 1)
						.requires(CoreItems.BROKEN_BRONZE_SHOVEL))
				.build(consumer, id("carpenter", "reclaim_bronze_shovel"));
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
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.PORTABLE_ALYZER)
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
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.KIT_PICKAXE)
						.pattern("###")
						.pattern(" X ")
						.pattern(" X ")
						.define('#', ForestryTags.Items.INGOTS_BRONZE)
						.define('X', Items.STICK))
				.build(consumer, id("carpenter", "kit_pickaxe"));
		new CarpenterRecipeBuilder()
				.setPackagingTime(20)
				.setBox(Ingredient.of(CoreItems.CARTON))
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.KIT_SHOVEL)
						.pattern(" # ")
						.pattern(" X ")
						.pattern(" X ")
						.define('#', ForestryTags.Items.INGOTS_BRONZE)
						.define('X', Items.STICK))
				.build(consumer, id("carpenter", "kit_shovel"));
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
						.define('#', CoreItems.WOOD_PULP))
				.build(consumer, id("carpenter", "paper"));
		new CarpenterRecipeBuilder()
				.setLiquid(new FluidStack(Fluids.WATER, 1000))
				.setBox(Ingredient.EMPTY)
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CARTON, 2)
						.pattern(" # ")
						.pattern("# #")
						.pattern(" # ")
						.define('#', CoreItems.WOOD_PULP))
				.build(consumer, id("carpenter", "carton"));

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
		crate(consumer, CrateItems.CRATED_PHOSPHOR.get(), Ingredient.of(CoreItems.PHOSPHOR));
		crate(consumer, CrateItems.CRATED_ASH.get(), Ingredient.of(CoreItems.ASH));
		crate(consumer, CrateItems.CRATED_TIN.get(), Ingredient.of(ForestryTags.Items.INGOTS_TIN));
		crate(consumer, CrateItems.CRATED_COPPER.get(), Ingredient.of(Items.COPPER_INGOT));
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
		crate(consumer, CrateItems.CRATED_COBBLESTONE.get(), Ingredient.of(Tags.Items.COBBLESTONE));
		crate(consumer, CrateItems.CRATED_DIRT.get(), Ingredient.of(Items.DIRT));
		crate(consumer, CrateItems.CRATED_GRASS_BLOCK.get(), Ingredient.of(Items.GRASS_BLOCK));
		crate(consumer, CrateItems.CRATED_STONE.get(), Ingredient.of(Tags.Items.STONE));
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
		crate(consumer, CrateItems.CRATED_OBSIDIAN.get(), Ingredient.of(Tags.Items.OBSIDIAN));
		crate(consumer, CrateItems.CRATED_NETHERRACK.get(), Ingredient.of(Tags.Items.NETHERRACK));
		crate(consumer, CrateItems.CRATED_SOUL_SAND.get(), Ingredient.of(Items.SOUL_SAND));
		crate(consumer, CrateItems.CRATED_SANDSTONE.get(), Ingredient.of(Tags.Items.SANDSTONE));
		crate(consumer, CrateItems.CRATED_NETHER_BRICKS.get(), Ingredient.of(Items.NETHER_BRICKS));
		crate(consumer, CrateItems.CRATED_MYCELIUM.get(), Ingredient.of(Items.MYCELIUM));
		crate(consumer, CrateItems.CRATED_GRAVEL.get(), Ingredient.of(Tags.Items.GRAVEL));
		crate(consumer, CrateItems.CRATED_OAK_SAPLING.get(), Ingredient.of(Items.OAK_SAPLING));
		crate(consumer, CrateItems.CRATED_BIRCH_SAPLING.get(), Ingredient.of(Items.BIRCH_SAPLING));
		crate(consumer, CrateItems.CRATED_JUNGLE_SAPLING.get(), Ingredient.of(Items.JUNGLE_SAPLING));
		crate(consumer, CrateItems.CRATED_SPRUCE_SAPLING.get(), Ingredient.of(Items.SPRUCE_SAPLING));
		crate(consumer, CrateItems.CRATED_ACACIA_SAPLING.get(), Ingredient.of(Items.ACACIA_SAPLING));
		crate(consumer, CrateItems.CRATED_DARK_OAK_SAPLING.get(), Ingredient.of(Items.DARK_OAK_SAPLING));

		crate(consumer, CrateItems.CRATED_BEESWAX.get(), Ingredient.of(CoreItems.BEESWAX));
		crate(consumer, CrateItems.CRATED_REFRACTORY_WAX.get(), Ingredient.of(CoreItems.REFRACTORY_WAX));

		crate(consumer, CrateItems.CRATED_POLLEN_CLUSTER_NORMAL.get(), Ingredient.of(ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.NORMAL)));
		crate(consumer, CrateItems.CRATED_POLLEN_CLUSTER_CRYSTALLINE.get(), Ingredient.of(ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.CRYSTALLINE)));
		crate(consumer, CrateItems.CRATED_PROPOLIS.get(), Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL)));
		crate(consumer, CrateItems.CRATED_HONEYDEW.get(), Ingredient.of(ApicultureItems.HONEYDEW));
		crate(consumer, CrateItems.CRATED_ROYAL_JELLY.get(), Ingredient.of(ApicultureItems.ROYAL_JELLY));

		for (EnumHoneyComb comb : EnumHoneyComb.VALUES) {
			crate(consumer, CrateItems.CRATED_BEE_COMBS.get(comb).get(), Ingredient.of(ApicultureItems.BEE_COMBS.get(comb)));
		}

		new CarpenterRecipeBuilder()
				.setPackagingTime(10)
				.setLiquid(new FluidStack(Fluids.WATER, 250))
				.setBox(Ingredient.EMPTY)
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MailItems.LETTERS.get(ItemLetter.Size.EMPTY, ItemLetter.State.FRESH).item())
						.pattern("###")
						.pattern("###")
						.define('#', CoreItems.WOOD_PULP))
				.build(consumer, id("carpenter", "letter_pulp"));

		wovenBackpack(consumer, "miner", BackpackItems.MINER_BACKPACK, BackpackItems.MINER_BACKPACK_T_2);
		wovenBackpack(consumer, "digger", BackpackItems.DIGGER_BACKPACK, BackpackItems.DIGGER_BACKPACK_T_2);
		wovenBackpack(consumer, "forester", BackpackItems.FORESTER_BACKPACK, BackpackItems.FORESTER_BACKPACK_T_2);
		wovenBackpack(consumer, "hunter", BackpackItems.HUNTER_BACKPACK, BackpackItems.HUNTER_BACKPACK_T_2);
		wovenBackpack(consumer, "adventurer", BackpackItems.ADVENTURER_BACKPACK, BackpackItems.ADVENTURER_BACKPACK_T_2);
		wovenBackpack(consumer, "builder", BackpackItems.BUILDER_BACKPACK, BackpackItems.BUILDER_BACKPACK_T_2);
	}

	private static void wovenBackpack(Consumer<FinishedRecipe> consumer, String id, FeatureItem<?> tier1, FeatureItem<?> tier2) {
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

	private static void crate(Consumer<FinishedRecipe> consumer, ItemCrated crated, Ingredient ingredient) {
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

	private static void registerCentrifuge(Consumer<FinishedRecipe> consumer) {
		new CentrifugeRecipeBuilder()
				.setProcessingTime(5)
				.setInput(Ingredient.of(Items.STRING))
				.product(0.15F, CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.SILK_WISP))
				.build(consumer, id("centrifuge", "string"));

		ItemStack honeyDrop = ApicultureItems.HONEY_DROP.stack();

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
				.product(0.7f, CoreItems.PHOSPHOR.stack(2))
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
				.product(1.0f, ApicultureItems.HONEYDEW.stack())
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
				.product(0.6f, ApicultureItems.HONEYDEW.stack())
				.product(0.2f, CoreItems.BEESWAX.stack())
				.product(0.3f, new ItemStack(Items.QUARTZ))
				.build(consumer, id("centrifuge", "mellow_comb"));
		new CentrifugeRecipeBuilder()
				.setProcessingTime(5)
				.setInput(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.SILKY)))
				.product(0.6f, CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.SILK_WISP, 1))
				.product(0.1f, ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1))
				.build(consumer, id("centrifuge", "silky_propolis"));
	}

	private static void registerFabricator(Consumer<FinishedRecipe> consumer) {
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
						.define('X', Items.COPPER_INGOT))
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
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.FLEXIBLE_CASING)
						.pattern("#E#")
						.pattern("B B")
						.pattern("#E#")
						.define('#', ForestryTags.Items.INGOTS_BRONZE)
						.define('B', Tags.Items.SLIMEBALLS)
						.define('E', Tags.Items.GEMS_EMERALD))
				.build(consumer, id("fabricator", "electron_tubes", "flexible_casing"));

		for (ForestryWoodType type : ForestryWoodType.values()) {
			addFireproofRecipes(consumer, type);
		}

		for (VanillaWoodType type : VanillaWoodType.values()) {
			addFireproofRecipes(consumer, type);
		}
	}

	private static void addFireproofRecipes(Consumer<FinishedRecipe> consumer, IWoodType type) {
		FluidStack liquidGlass = ForestryFluids.GLASS.getFluid(500);

		List<WoodBlockKind> logLike = List.of(WoodBlockKind.LOG, WoodBlockKind.WOOD, WoodBlockKind.STRIPPED_LOG, WoodBlockKind.STRIPPED_WOOD);

		for (WoodBlockKind woodKind : logLike) {
			try {
				new FabricatorRecipeBuilder()
						.setPlan(Ingredient.EMPTY)
						.setMolten(liquidGlass)
						.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TreeManager.woodAccess.getBlock(type, woodKind, true).getBlock())
								.pattern(" # ")
								.pattern("#X#")
								.pattern(" # ")
								.define('#', CoreItems.REFRACTORY_WAX)
								.define('X', TreeManager.woodAccess.getBlock(type, woodKind, false).getBlock()))
						.build(consumer, id("fabricator", "fireproof", woodKind.getSerializedName(), type.toString()));
			} catch (IllegalStateException ignored) {
			}
		}

		new FabricatorRecipeBuilder()
				.setPlan(Ingredient.EMPTY)
				.setMolten(liquidGlass)
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TreeManager.woodAccess.getBlock(type, WoodBlockKind.PLANKS, true).getBlock(), 5)
						.pattern("X#X")
						.pattern("#X#")
						.pattern("X#X")
						.define('#', CoreItems.REFRACTORY_WAX)
						.define('X', TreeManager.woodAccess.getBlock(type, WoodBlockKind.PLANKS, false).getBlock()))
				.build(consumer, id("fabricator", "fireproof", "planks", type.toString()));
	}

	private static void registerFabricatorSmelting(Consumer<FinishedRecipe> consumer) {
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
	}

	private static void registerFermenter(Consumer<FinishedRecipe> consumer) {
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Items.BROWN_MUSHROOM))
				.setFermentationValue(Preference.FERMENTED_MUSHROOM)
				.setModifier(1.5f)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(ForestryFluids.HONEY.getFluid(1))
				.build(consumer, id("fermenter", "brown_mushroom_honey"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Items.BROWN_MUSHROOM))
				.setFermentationValue(Preference.FERMENTED_MUSHROOM)
				.setModifier(1.5f)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(ForestryFluids.JUICE.getFluid(1))
				.build(consumer, id("fermenter", "brown_mushroom_juice"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Items.RED_MUSHROOM))
				.setFermentationValue(Preference.FERMENTED_MUSHROOM)
				.setModifier(1.5f)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(ForestryFluids.HONEY.getFluid(1))
				.build(consumer, id("fermenter", "red_mushroom_honey"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Items.RED_MUSHROOM))
				.setFermentationValue(Preference.FERMENTED_MUSHROOM)
				.setModifier(1.5f)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(ForestryFluids.JUICE.getFluid(1))
				.build(consumer, id("fermenter", "red_mushroom_juice"));

		FluidStack shortMead = ForestryFluids.SHORT_MEAD.getFluid(1);
		FluidStack honey = ForestryFluids.HONEY.getFluid(1);

		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(ApicultureItems.HONEYDEW))
				.setFermentationValue(500)
				.setModifier(1.0f)
				.setOutput(shortMead.getFluid())
				.setFluidResource(honey)
				.build(consumer, id("fermenter", "honeydew"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(ItemTags.SAPLINGS))
				.setFermentationValue(Preference.FERMENTED_SAPLING)
				.setModifier(1)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(new FluidStack(Fluids.WATER, 1000))
				.build(consumer, id("fermenter", "sapling"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Items.CACTUS))
				.setFermentationValue(Preference.FERMENTED_CACTI)
				.setModifier(1)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(new FluidStack(Fluids.WATER, 1000))
				.build(consumer, id("fermenter", "cactus"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Tags.Items.CROPS_WHEAT))
				.setFermentationValue(Preference.FERMENTED_WHEAT)
				.setModifier(1)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(new FluidStack(Fluids.WATER, 1000))
				.build(consumer, id("fermenter", "wheat"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Tags.Items.CROPS_POTATO))
				.setFermentationValue(2 * Preference.FERMENTED_WHEAT)
				.setModifier(1)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(new FluidStack(Fluids.WATER, 1000))
				.build(consumer, id("fermenter", "potato"));
		new FermenterRecipeBuilder()
				.setResource(Ingredient.of(Items.SUGAR_CANE))
				.setFermentationValue(Preference.FERMENTED_CANE)
				.setModifier(1)
				.setOutput(ForestryFluids.BIOMASS.getFluid())
				.setFluidResource(new FluidStack(Fluids.WATER, 1000))
				.build(consumer, id("fermenter", "sugar_cane"));
	}

	private static void registerHygroregulator(Consumer<FinishedRecipe> consumer) {
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

	private static void registerMoistener(Consumer<FinishedRecipe> consumer) {
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

	private static void registerSqueezerContainer(Consumer<FinishedRecipe> consumer) {
		new SqueezerContainerRecipeBuilder()
				.setProcessingTime(10)
				.setEmptyContainer(FluidsItems.CONTAINERS.stack(EnumContainerType.CAN))
				.setRemnants(CoreItems.INGOT_TIN.stack())
				.setRemnantsChance(0.05f)
				.build(consumer, id("squeezer", "container", "can"));
		new SqueezerContainerRecipeBuilder()
				.setProcessingTime(10)
				.setEmptyContainer(FluidsItems.CONTAINERS.stack(EnumContainerType.CAPSULE))
				.setRemnants(CoreItems.BEESWAX.stack())
				.setRemnantsChance(0.10f)
				.build(consumer, id("squeezer", "container", "capsule"));
		new SqueezerContainerRecipeBuilder()
				.setProcessingTime(10)
				.setEmptyContainer(FluidsItems.CONTAINERS.stack(EnumContainerType.REFRACTORY))
				.setRemnants(CoreItems.REFRACTORY_WAX.stack())
				.setRemnantsChance(0.10f)
				.build(consumer, id("squeezer", "container", "refractory"));
	}

	private static void registerSqueezer(Consumer<FinishedRecipe> consumer) {
		FluidStack honeyDropFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP);

		new SqueezerRecipeBuilder()
				.setProcessingTime(10)
				.setResources(NonNullList.withSize(1, Ingredient.of(ApicultureItems.HONEY_DROP)))
				.setFluidOutput(honeyDropFluid)
				.setRemnants(ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1))
				.setRemnantsChance(5 / 100f)
				.build(consumer, id("squeezer", "honey_drop"));
		new SqueezerRecipeBuilder()
				.setProcessingTime(10)
				.setResources(NonNullList.withSize(1, Ingredient.of(ApicultureItems.HONEYDEW)))
				.setFluidOutput(honeyDropFluid)
				.build(consumer, id("squeezer", "honey_dew"));
		new SqueezerRecipeBuilder()
				.setProcessingTime(10)
				.setResources(Util.make(NonNullList.create(), (ingredients) -> {
					ingredients.add(Ingredient.of(CoreItems.PHOSPHOR));
					ingredients.add(Ingredient.of(CoreItems.PHOSPHOR));
					ingredients.add(Ingredient.of(Items.SAND, Items.RED_SAND));
				}))
				.setFluidOutput(new FluidStack(Fluids.LAVA, 2000))
				.build(consumer, id("squeezer", "lava_sand"));
		new SqueezerRecipeBuilder()
				.setProcessingTime(10)
				.setResources(Util.make(NonNullList.create(), (ingredients) -> {
					ingredients.add(Ingredient.of(CoreItems.PHOSPHOR));
					ingredients.add(Ingredient.of(CoreItems.PHOSPHOR));
					ingredients.add(Ingredient.of(Items.DIRT, Items.COBBLESTONE));
				}))
				.setFluidOutput(new FluidStack(Fluids.LAVA, 1600))
				.build(consumer, id("squeezer", "lava"));

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
				.setFluidOutput(new FluidStack(seedOil, seedOilMultiplier * 5))
				.setRemnants(mulch)
				.setRemnantsChance(0.05F)
				.build(consumer, id("squeezer", "fruit", "cherry"));
		new SqueezerRecipeBuilder()
				.setProcessingTime(60)
				.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.WALNUT)))
				.setFluidOutput(new FluidStack(seedOil, seedOilMultiplier * 18))
				.setRemnants(mulch)
				.setRemnantsChance(0.05F)
				.build(consumer, id("squeezer", "fruit", "walnut"));
		new SqueezerRecipeBuilder()
				.setProcessingTime(70)
				.setResources(NonNullList.withSize(1, Ingredient.of(ForestryTags.Items.CHESTNUT)))
				.setFluidOutput(new FluidStack(seedOil, seedOilMultiplier * 22))
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
	}

	private static void registerStill(Consumer<FinishedRecipe> consumer) {
		FluidStack biomass = ForestryFluids.BIOMASS.getFluid(STILL_DESTILLATION_INPUT);
		FluidStack ethanol = ForestryFluids.BIO_ETHANOL.getFluid(STILL_DESTILLATION_OUTPUT);

		new StillRecipeBuilder()
				.setTimePerUnit(STILL_DESTILLATION_DURATION)
				.setInput(biomass)
				.setOutput(ethanol)
				.build(consumer, id("still", "ethanol"));
	}

	private static ResourceLocation id(String... path) {
		return new ResourceLocation("forestry", String.join("/", path));
	}
}
