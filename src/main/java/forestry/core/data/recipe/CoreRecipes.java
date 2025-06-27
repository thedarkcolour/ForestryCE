package forestry.core.data.recipe;

import forestry.api.ForestryConstants;
import forestry.api.ForestryTags;
import forestry.api.core.Product;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.apiculture.recipes.HygroregulatorRecipe;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.blocks.EnumResourceType;
import forestry.core.circuits.CircuitBoard;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.config.Constants;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreDataComponents;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.fluids.ForestryFluids;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.energy.blocks.EngineBlockType;
import forestry.energy.features.EnergyBlocks;
import forestry.factory.recipes.*;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.lepidopterology.recipe.ButterflyMatingRecipe;
import forestry.mail.blocks.BlockTypeMail;
import forestry.mail.features.MailBlocks;
import forestry.mail.features.MailItems;
import forestry.mail.items.EnumStampDefinition;
import forestry.mail.items.ItemLetter;
import forestry.mail.items.ItemStamp;
import forestry.modules.features.FeatureItem;
import forestry.sorting.features.SortingBlocks;
import forestry.storage.features.CrateItems;
import forestry.worktable.features.WorktableBlocks;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import thedarkcolour.modkit.data.MKRecipeProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static thedarkcolour.modkit.data.MKRecipeProvider.ingredient;

public class CoreRecipes {
	// todo it might be cool to have a custom ingredient that checks for the fluid capability, so stuff like Thermal tanks could be used
	static Ingredient getContainer(EnumContainerType type, Fluid fluid) {
		ItemStack container = FluidsItems.CONTAINERS.stack(type);
		IFluidHandlerItem fluidHandlerCap = container.getCapability(Capabilities.FluidHandler.ITEM);
		fluidHandlerCap.fill(new FluidStack(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
		return DataComponentIngredient.of(false, container);
	}

	public static void addRecipes(RecipeOutput output, MKRecipeProvider recipes) {
		// Vanilla recipe types
		ArboricultureRecipes.registerArboricultureRecipes(output, recipes);
		ApicultureRecipes.registerApicultureRecipes(recipes);
		ApicultureRecipes.registerFoodRecipes(recipes);
		StorageRecipes.registerBackpackRecipes(output, recipes);
		ArboricultureRecipes.registerCharcoalRecipes(recipes);
		registerCoreRecipes(recipes);
		CultivationRecipes.registerCultivationRecipes(recipes);
		FactoryRecipes.registerFactoryRecipes(recipes);
		CultivationRecipes.registerFarmingRecipes(recipes);
		registerFluidsRecipes(recipes);
		registerLepidopterologyRecipes(recipes);
		registerMailRecipes(recipes);
		registerSortingRecipes(recipes);
		registerWorktableRecipes(recipes);
		registerEnergyRecipes(recipes);

		// Forestry recipe types
		registerCarpenter(output, recipes);
		registerCentrifuge(output);
		FactoryRecipes.registerFabricator(output, recipes);
		FactoryRecipes.registerFabricatorSmelting(output);
		registerFermenter(output);
		registerHygroregulator(output);
		CultivationRecipes.registerMoistener(output);
		registerSqueezerContainer(output);
		registerSqueezer(output);
		registerStill(output);
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

		recipes.shapedCrafting("compressed_ice_shards", RecipeCategory.MISC, Items.ICE, 1, recipe -> {
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD));
			recipe.pattern("##");
			recipe.pattern("##");
		});

		recipes.shapedCrafting("honey_drop_block", RecipeCategory.MISC, Items.HONEY_BLOCK, 1, recipe -> {
			recipe.define('V', ApicultureItems.HONEY_DROP);
			recipe.pattern("VVV");
			recipe.pattern("V V");
			recipe.pattern("VVV");
		});

		recipes.shapedCrafting("phosphor_torches", RecipeCategory.MISC, Items.TORCH, 6, recipe -> {
			recipe.define('P', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHOR));
			recipe.define('|', Tags.Items.RODS_WOODEN);
			recipe.pattern(" P ");
			recipe.pattern(" | ");
		});

		recipes.shapedCrafting("beeswax_candles", RecipeCategory.MISC, Items.CANDLE, 1, recipe -> {
			recipe.define('|', Tags.Items.STRINGS);
			recipe.define('^', CoreItems.BEESWAX);
			recipe.pattern(" | ");
			recipe.pattern(" ^ ");
		});

		// Books
		recipes.shapelessCrafting("foresters_manual_honeydrop", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, ApicultureItems.HONEY_DROP);
		recipes.shapelessCrafting("foresters_manual_sapling", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, ItemTags.SAPLINGS);
		recipes.shapelessCrafting("foresters_manual_butterfly", RecipeCategory.MISC, CoreItems.FORESTERS_MANUAL, 1, Items.BOOK, LepidopterologyItems.BUTTERFLY_GE);
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

	private static void registerFluidsRecipes(MKRecipeProvider recipes) {
		for (EnumContainerType containerType : EnumContainerType.values()) {
			recipes.shapedCrafting("cake_" + containerType.identifier(), RecipeCategory.FOOD, Items.CAKE, recipe -> {
				recipe.define('A', getContainer(containerType, NeoForgeMod.MILK.get()));
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
			recipe.define('#', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', LepidopterologyItems.BUTTERFLY_GE);
			recipe.define('Y', Tags.Items.CHESTS_WOODEN);
			recipe.pattern(" # ");
			recipe.pattern("XYX");
			recipe.pattern("XXX");
		});
		recipes.special("butterfly_mating", ButterflyMatingRecipe::new);
	}

	private static void registerMailRecipes(MKRecipeProvider recipes) {
		recipes.shapelessCrafting(RecipeCategory.MISC, MailItems.CATALOGUE, 1, Items.BOOK, ForestryTags.Items.STAMPS);
		Ingredient sealant = Ingredient.fromValues(Stream.of(new Ingredient.TagValue(ForestryTags.Items.PROPOLIS), new Ingredient.TagValue(Tags.Items.SLIME_BALLS)));
		recipes.shapelessCrafting(RecipeCategory.MISC, MailItems.LETTERS.get(ItemLetter.Size.EMPTY, ItemLetter.State.FRESH), 1, Items.PAPER, sealant);

		recipes.shapedCrafting(RecipeCategory.MISC, MailBlocks.BASE.get(BlockTypeMail.MAILBOX).block(), recipe -> {
			recipe.define('#', ForestryTags.Items.INGOTS_TIN);
			recipe.define('X', Tags.Items.CHESTS_WOODEN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.pattern(" # ");
			recipe.pattern("#Y#");
			recipe.pattern("XXX");
		});

		Ingredient emptiedLetter = Ingredient.fromValues(MailItems.LETTERS.getRowFeatures(ItemLetter.Size.EMPTY).stream().map(feature -> new Ingredient.ItemValue(feature.stack())));
		recipes.shapedCrafting("paper_from_letters", RecipeCategory.MISC, Items.PAPER, recipe -> {
			recipe.define('#', emptiedLetter);
			recipe.pattern(" # ");
			recipe.pattern(" # ");
			recipe.pattern(" # ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, MailBlocks.BASE.get(BlockTypeMail.TRADE_STATION).block(), recipe -> {
			recipe.define('#', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.BRONZE));
			recipe.define('X', Tags.Items.CHESTS_WOODEN);
			recipe.define('Y', CoreItems.STURDY_CASING);
			recipe.define('Z', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.IRON));
			recipe.define('W', DataComponentIngredient.of(false, CoreDataComponents.CIRCUIT_BOARD.value(), CircuitBoard.empty(EnumCircuitBoardType.REFINED), CoreItems.CIRCUITBOARDS.item(EnumCircuitBoardType.REFINED)));
			recipe.pattern("Z#Z");
			recipe.pattern("#Y#");
			recipe.pattern("XWX");
		});

		Ingredient glue = Ingredient.fromValues(Stream.of(
			new Ingredient.TagValue(ForestryTags.Items.DROP_HONEY),
			new Ingredient.TagValue(Tags.Items.SLIME_BALLS)
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
		Ingredient ing = Ingredient.fromValues(Stream.of(
			new Ingredient.ItemValue(LepidopterologyItems.CATERPILLAR.stack()),
			new Ingredient.ItemValue(ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL)),
			new Ingredient.TagValue(ForestryTags.Items.FORESTRY_FRUITS)
		));

		recipes.shapedCrafting(RecipeCategory.MISC, SortingBlocks.FILTER.block(), 2, recipe -> {
			recipe.define('B', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('D', Tags.Items.GEMS_DIAMOND);
			recipe.define('F', ing);
			recipe.define('W', ItemTags.PLANKS);
			recipe.define('G', Tags.Items.GLASS_BLOCKS);
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
			recipe.define('I', Tags.Items.GLASS_BLOCKS);
			recipe.define('Q', ForestryTags.Items.GEARS_COPPER);
			recipe.define('D', Items.PISTON);
			recipe.define('C', Items.CLOCK);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDC");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS), recipe -> {
			recipe.define('P', ForestryTags.Items.INGOTS_BRONZE);
			recipe.define('I', Tags.Items.GLASS_BLOCKS);
			recipe.define('Q', ForestryTags.Items.GEARS_BRONZE);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});

		recipes.shapedCrafting(RecipeCategory.MISC, EnergyBlocks.ENGINES.get(EngineBlockType.PEAT), recipe -> {
			recipe.define('P', Tags.Items.INGOTS_COPPER);
			recipe.define('I', Tags.Items.GLASS_BLOCKS);
			recipe.define('Q', ForestryTags.Items.GEARS_COPPER);
			recipe.define('D', Items.PISTON);
			recipe.pattern("PPP");
			recipe.pattern(" I ");
			recipe.pattern("QDQ");
		});
	}

	private static void registerCarpenter(RecipeOutput output, MKRecipeProvider recipes) {
		carpenter(output, recipes, 50, ForestryFluids.SEED_OIL.ingredient(250), Ingredient.EMPTY, CoreItems.IMPREGNATED_CASING, 1, recipe -> {
			recipe.pattern("###");
			recipe.pattern("# #");
			recipe.pattern("###");
			recipe.define('#', ItemTags.LOGS);
		});
		carpenter(output, recipes, 50, ForestryFluids.SEED_OIL.ingredient(250), Ingredient.EMPTY, CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).item(), 1, recipe -> {
			recipe.pattern("#  ");
			recipe.pattern("###");
			recipe.pattern("# #");
			recipe.define('#', ItemTags.PLANKS);
		});
		carpenter(output, recipes, 50, ForestryFluids.SEED_OIL.ingredient(100), Ingredient.EMPTY, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.IMPREGNATED_STICK), 2, recipe -> {
			recipe.pattern("#");
			recipe.pattern("#");
			recipe.define('#', ItemTags.LOGS);
		});
		carpenterShapeless(output, recipes, 5, SizedFluidIngredient.of(Fluids.WATER, 250), Ingredient.EMPTY, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), 4, shapeless -> shapeless);
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), 4)
				.requires(ItemTags.LOGS))
			.build(output, id("carpenter", "wood_pulp"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreBlocks.HUMUS, 9)
				.pattern("###")
				.pattern("#X#")
				.pattern("###")
				.define('#', Items.DIRT)
				.define('X', CoreItems.MULCH))
			.build(output, id("carpenter", "humus"));
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
			.build(output, id("carpenter", "bog_earth"));
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
			.build(output, id("carpenter", "hardened_casing"));
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
			.build(output, id("carpenter", "iodine_charge"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.DISSIPATION_CHARGE)
				.pattern("Z#Z")
				.pattern("#Y#")
				.pattern("X#X")
				.define('#', ApicultureItems.ROYAL_JELLY)
				.define('X', Items.GUNPOWDER)
				.define('Y', FluidsItems.CONTAINERS.get(EnumContainerType.CAN))
				.define('Z', ApicultureItems.HONEYDEW))
			.build(output, id("carpenter", "dissipation_charge"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(100)
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.ENDER_PEARL)
				.pattern(" # ")
				.pattern("###")
				.pattern(" # ")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PULSATING_MESH)))
			.build(output, id("carpenter", "ender_pearl"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(10)
			.setLiquid(new FluidStack(Fluids.WATER, 500))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK))
				.pattern("XX")
				.pattern("XX")
				.define('X', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP)))
			.build(output, id("carpenter", "woven_silk"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.INGOT_BRONZE, 2)
				.requires(CoreItems.BROKEN_BRONZE_PICKAXE))
			.build(output, id("carpenter", "reclaim_bronze_pickaxe"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CoreItems.INGOT_BRONZE, 1)
				.requires(CoreItems.BROKEN_BRONZE_SHOVEL))
			.build(output, id("carpenter", "reclaim_bronze_shovel"));
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
			.build(output, id("carpenter", "scented_paneling"));
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
			.build(output, id("carpenter", "portable_analyzer"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.KIT_PICKAXE)
				.pattern("###")
				.pattern(" X ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(output, id("carpenter", "kit_pickaxe"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(20)
			.setBox(Ingredient.of(CoreItems.CARTON))
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CoreItems.KIT_SHOVEL)
				.pattern(" # ")
				.pattern(" X ")
				.pattern(" X ")
				.define('#', ForestryTags.Items.INGOTS_BRONZE)
				.define('X', Items.STICK))
			.build(output, id("carpenter", "kit_shovel"));
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
			.build(output, id("carpenter", "soldering_iron"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER)
				.pattern("#")
				.pattern("#")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOOD_PULP)))
			.build(output, id("carpenter", "paper"));
		new CarpenterRecipeBuilder()
			.setLiquid(new FluidStack(Fluids.WATER, 1000))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CARTON, 2)
				.pattern(" # ")
				.pattern("# #")
				.pattern(" # ")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOOD_PULP)))
			.build(output, id("carpenter", "carton"));

		for (EnumStampDefinition stamp : EnumStampDefinition.VALUES) {
			FeatureItem<ItemStamp> item = MailItems.STAMPS.get(stamp);

			new CarpenterRecipeBuilder()
				.setLiquid(ForestryFluids.SEED_OIL.getFluid(300))
				.setBox(Ingredient.EMPTY)
				.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item, 9)
					.pattern("###")
					.pattern("PPP")
					.define('#', stamp.getCraftingIngredient())
					.define('P', Items.PAPER))
				.build(output, id("carpenter", item.getName()));
		}

		ItemStack basic = CoreItems.CIRCUITBOARDS.stack(EnumCircuitBoardType.BASIC);
		ItemStack enhanced = CoreItems.CIRCUITBOARDS.stack(EnumCircuitBoardType.ENHANCED);
		ItemStack refined = CoreItems.CIRCUITBOARDS.stack(EnumCircuitBoardType.REFINED);
		ItemStack intricate = CoreItems.CIRCUITBOARDS.stack(EnumCircuitBoardType.INTRICATE);

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
			.build(output, id("carpenter", "circuits", "basic"));
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
			.build(output, id("carpenter", "circuits", "enhanced"));
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
			.build(output, id("carpenter", "circuits", "refined"));
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
			.build(output, id("carpenter", "circuits", "intricate"));
		new CarpenterRecipeBuilder()
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.CANDLE, 4)
				.pattern("# #")
				.pattern(" X ")
				.pattern("# #")
				.define('#', CoreItems.BEESWAX)
				.define('X', Items.STRING))
			.build(output, id("carpenter", "candles"));

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
			.build(output, id("carpenter", "crates", "empty"));

		new CarpenterRecipeBuilder()
			.setPackagingTime(10)
			.setLiquid(new FluidStack(Fluids.WATER, 250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MailItems.LETTERS.get(ItemLetter.Size.EMPTY, ItemLetter.State.FRESH).item())
				.pattern("###")
				.pattern("###")
				.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOOD_PULP)))
			.build(output, id("carpenter", "letter_pulp"));
	}

	protected static void carpenter(RecipeOutput output, MKRecipeProvider recipes, int packingTime, @Nullable SizedFluidIngredient inputFluid, Ingredient box, ItemLike result, int resultCount, Consumer<ShapedRecipeBuilder> pattern) {
		recipes.pushRecipeOutput(
			// the recipe is passed in by newOutput, letting us obtain the finished recipe instance from ModKit
			(id, recipe) -> output.accept(id("carpenter", MKRecipeProvider.path(result)), new CarpenterRecipe(packingTime, Optional.ofNullable(inputFluid), box, (CraftingRecipe) recipe), null),
			// create a shaped recipe with the new output, which ModKit will pass into the above function
			newOutput -> recipes.shapedCrafting(RecipeCategory.MISC, result, resultCount, pattern)
		);
	}

	private static void carpenterShapeless(RecipeOutput output, MKRecipeProvider recipes, int packingTime, @Nullable SizedFluidIngredient inputFluid, Ingredient box, ItemLike result, int resultCount, Consumer<MKRecipeProvider> shapeless) {
		recipes.pushRecipeOutput(
			(id, recipe) -> output.accept(id("carpenter", MKRecipeProvider.path(recipe.getResultItem(null).getItem())), new CarpenterRecipe(packingTime, Optional.ofNullable(inputFluid), box, (CraftingRecipe) recipe), null),
			newOutput -> shapeless.accept(recipes)
		);
	}

	private static void registerCentrifuge(RecipeOutput output) {
		Item honeyDrop = ApicultureItems.HONEY_DROP.item();

		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.HONEY), products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
			products.accept(0.9f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.COCOA), products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
			products.accept(0.5f, Items.COCOA_BEANS);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.SIMMERING), products -> {
			products.accept(1.0f, CoreItems.REFRACTORY_WAX);
			products.accept(0.7f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.STRINGY), products -> {
			products.accept(1.0f, ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL));
			products.accept(0.4f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.DRIPPING), products -> {
			products.accept(1.0f, ApicultureItems.HONEYDEW);
			products.accept(0.4f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.FROZEN), products -> {
			products.accept(0.8f, CoreItems.BEESWAX);
			products.accept(0.7f, honeyDrop);
			products.accept(0.4f, Items.SNOWBALL);
			products.accept(0.2f, ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.CRYSTALLINE));
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.SILKY), products -> {
			products.accept(1.0f, honeyDrop);
			products.accept(0.8f, ApicultureItems.PROPOLIS.get(EnumPropolis.SILKY));
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.PARCHED), products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
			products.accept(0.9f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.MYSTERIOUS), products -> {
			products.accept(1.0f, ApicultureItems.PROPOLIS.get(EnumPropolis.PULSATING));
			products.accept(0.4f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.POWDERY), products -> {
			products.accept(0.2f, honeyDrop);
			products.accept(0.2f, CoreItems.BEESWAX);
			products.accept(0.9f, Items.GUNPOWDER);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.WHEATEN), products -> {
			products.accept(0.2f, honeyDrop);
			products.accept(0.2f, CoreItems.BEESWAX);
			products.accept(0.8f, Items.WHEAT);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.MOSSY), products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
			products.accept(0.9f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.KAOLIN), products -> {
			products.accept(1.0f, Items.CLAY_BALL);
			products.accept(0.9f, honeyDrop);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.MELLOW), products -> {
			products.accept(0.6f, ApicultureItems.HONEYDEW);
			products.accept(0.2f, CoreItems.BEESWAX);
			products.accept(0.3f, Items.QUARTZ);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.VINTAGE), products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
			products.accept(0.9f, ApicultureItems.HONEYDEW);
		});
		centrifuge(output, 20, ApicultureItems.BEE_COMBS.get(EnumHoneyComb.SCULKEN), products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
			products.accept(0.9f, ApicultureItems.EXPERIENCE_DROP);
			products.accept(0.2f, Items.SCULK);
		});
		centrifuge(output, 5, ApicultureItems.PROPOLIS.get(EnumPropolis.SILKY), products -> {
			products.accept(0.6f, CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SILK_WISP));
			products.accept(0.1f, ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL));
		});

		centrifuge(output, 20, Items.HONEYCOMB, products -> {
			products.accept(1.0f, CoreItems.BEESWAX);
		});
	}

	private static void centrifuge(RecipeOutput output, int processingTime, ItemLike input, Consumer<BiConsumer<Float, ItemLike>> products) {
		ArrayList<Product> productsList = new ArrayList<>();
		products.accept((chance, item) -> productsList.add(Product.of(item.asItem(), chance)));
		output.accept(id("centrifuge", MKRecipeProvider.path(input)), new CentrifugeRecipe(processingTime, Ingredient.of(input), productsList), null);
	}

	private static void registerFermenter(RecipeOutput output) {
		// Apiculture
		fermenter(output, "honeydew", Ingredient.of(ApicultureItems.HONEYDEW), FluidIngredient.tag(Tags.Fluids.HONEY), 500, 1f, ForestryFluids.HONEY.getFluid());
		// Arboriculture
		addFermenterRecipes(output, "sapling", Ingredient.of(ItemTags.SAPLINGS), 250);
		// Factory
		addFermenterRecipes(output, "cactus", Ingredient.of(Items.CACTUS), 50);
		addFermenterRecipes(output, "wheat", Ingredient.of(Tags.Items.CROPS_WHEAT), 50);
		addFermenterRecipes(output, "potato", Ingredient.of(Tags.Items.CROPS_POTATO), 100);
		addFermenterRecipes(output, "sugar_cane", Ingredient.of(Items.SUGAR_CANE), 50);
		addFermenterRecipes(output, "mushroom", Ingredient.of(Tags.Items.MUSHROOMS), 50);
	}

	private static void addFermenterRecipes(RecipeOutput output, String name, Ingredient input, int fermentationValue) {
		Fluid outputFluid = ForestryFluids.BIOMASS.getFluid();

		fermenter(output, name, input, FluidIngredient.tag(FluidTags.WATER), fermentationValue, 1f, outputFluid);
		// todo Juice tag?
		fermenter(output, name + "_juice", input, FluidIngredient.single(ForestryFluids.JUICE.getFluid()), fermentationValue, 1.5f, outputFluid);
		fermenter(output, name + "_honey", input, FluidIngredient.tag(Tags.Fluids.HONEY), fermentationValue, 1.5f, outputFluid);
	}

	private static void fermenter(RecipeOutput output, String name, Ingredient input, FluidIngredient fluidInput, int fermentationValue, float modifier, Fluid result) {
		output.accept(id("fermenter", name), new FermenterRecipe(input, fluidInput, fermentationValue, modifier, result), null);
	}

	private static void registerHygroregulator(RecipeOutput output) {
		hygroregulator(output, "water", FluidIngredient.tag(FluidTags.WATER), 0, -1, 1);
		hygroregulator(output, "lava", FluidIngredient.tag(FluidTags.LAVA), 0, 1, -1);
		hygroregulator(output, "ice", FluidIngredient.single(ForestryFluids.ICE.getFluid()), 10, -2, 2);
	}

	private static void hygroregulator(RecipeOutput output, String name, FluidIngredient input, int retainTime, int temperatureSteps, int humiditySteps) {
		output.accept(id("hygroregulator", name), new HygroregulatorRecipe(new SizedFluidIngredient(input, 1), retainTime, (byte) temperatureSteps, (byte) humiditySteps), null);
	}

	private static void registerSqueezerContainer(RecipeOutput output) {
		squeezerContainer(output, EnumContainerType.CAN, CoreItems.INGOT_TIN, 0.05f);
		squeezerContainer(output, EnumContainerType.CAPSULE, CoreItems.BEESWAX, 0.10f);
		squeezerContainer(output, EnumContainerType.REFRACTORY, CoreItems.REFRACTORY_WAX, 0.10f);
	}

	private static void squeezerContainer(RecipeOutput output, EnumContainerType type, ItemLike remnants, float remnantsChance) {
		output.accept(id("squeezer_container", type.identifier()), new SqueezerContainerRecipe(FluidsItems.CONTAINERS.stack(type), 10, new ItemStack(remnants), remnantsChance), null);
	}

	private static void registerSqueezer(RecipeOutput output) {
		FluidStack honeyDropFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP);
		FluidStack honeyBlockFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP * 8);

		squeezer(output, "honey_drop", 10, List.of(Ingredient.of(ApicultureItems.HONEY_DROP)), honeyDropFluid, ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1), 5 / 100f);
		squeezer(output, "sponge_comb", 10, List.of(Ingredient.of(ApicultureItems.BEE_COMBS.stack(EnumHoneyComb.SPONGE))), honeyDropFluid, new ItemStack(Items.SPONGE), 2 / 100f);
		squeezer(output, "honey_block", 60, List.of(Ingredient.of(Items.HONEY_BLOCK)), honeyBlockFluid);
		squeezer(output, "honey_dew", 10, List.of(Ingredient.of(ApicultureItems.HONEYDEW)), honeyDropFluid);
		squeezer(output, "lava_sand", 20, List.of(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.VOLCANIC)), Ingredient.of(Items.SAND, Items.RED_SAND)), new FluidStack(Fluids.LAVA, 500));
		squeezer(output, "lava", 30, List.of(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.VOLCANIC)), Ingredient.of(Items.COBBLESTONE)), new FluidStack(Fluids.LAVA, 500));
		squeezer(output, "lava_magma", 20, List.of(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.VOLCANIC)), Ingredient.of(Items.MAGMA_BLOCK)), new FluidStack(Fluids.LAVA, 1000));

		int seedOilAmount = 10;

		squeezer(output, "seeds", 10, List.of(Ingredient.of(Tags.Items.SEEDS)), ForestryFluids.SEED_OIL.getFluid(seedOilAmount));

		float mulchMultiplier = 0.2f;
		int juiceMultiplier = 200;

		squeezer(output, "mulch", 10, List.of(Ingredient.of(Items.APPLE, Items.CARROT)), ForestryFluids.JUICE.getFluid(juiceMultiplier), CoreItems.MULCH.stack(), mulchMultiplier);
		squeezer(output, "cactus", 10, List.of(Ingredient.of(Items.CACTUS)), new FluidStack(Fluids.WATER, 500));
		squeezer(output, "ice", 10, List.of(Ingredient.of(Items.SNOWBALL), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD))), ForestryFluids.ICE.getFluid(4000));

		int seedOilMultiplier = 10;

		ItemStack mulch = new ItemStack(CoreItems.MULCH);
		Fluid seedOil = ForestryFluids.SEED_OIL.getFluid();
		Fluid juice = ForestryFluids.JUICE.getFluid();

		squeezer(output, "cherry", 20, List.of(Ingredient.of(ForestryTags.Items.CHERRY)), new FluidStack(seedOil, seedOilMultiplier * 5), mulch, 0.05F);
		squeezer(output, "walnut", 60, List.of(Ingredient.of(ForestryTags.Items.WALNUT)), new FluidStack(seedOil, seedOilMultiplier * 18), mulch, 0.05F);
		squeezer(output, "chestnut", 70, List.of(Ingredient.of(ForestryTags.Items.CHESTNUT)), new FluidStack(seedOil, seedOilMultiplier * 22), mulch, 0.02F);
		squeezer(output, "lemon", 10, List.of(Ingredient.of(ForestryTags.Items.LEMON)), new FluidStack(juice, juiceMultiplier * 2), mulch, mulchMultiplier / 2f);
		squeezer(output, "plum", 10, List.of(Ingredient.of(ForestryTags.Items.PLUM)), new FluidStack(juice, juiceMultiplier / 2), mulch, mulchMultiplier * 3f);
		squeezer(output, "papaya", 10, List.of(Ingredient.of(ForestryTags.Items.PAPAYA)), new FluidStack(juice, juiceMultiplier * 3), mulch, mulchMultiplier / 2f);
		squeezer(output, "dates", 10, List.of(Ingredient.of(ForestryTags.Items.DATE)), new FluidStack(juice, juiceMultiplier / 4), mulch, mulchMultiplier);
	}

	private static void squeezer(RecipeOutput output, String id, int processingTime, List<Ingredient> inputs, FluidStack result) {
		squeezer(output, id, processingTime, inputs, result, ItemStack.EMPTY, 0f);
	}

	private static void squeezer(RecipeOutput output, String id, int processingTime, List<Ingredient> inputs, FluidStack result, ItemStack remnants, float remnantsChance) {
		output.accept(id("squeezer", id), new SqueezerRecipe(processingTime, inputs, result, remnants, remnantsChance), null);
	}

	private static void registerStill(RecipeOutput output) {
		output.accept(id("still", "ethanol"), new StillRecipe(
			100,
			ForestryFluids.BIOMASS.ingredient(10),
			ForestryFluids.BIO_ETHANOL.getFluid(3)
		), null);
	}

	static ResourceLocation id(String... path) {
		return ForestryConstants.forestry(String.join("/", path));
	}

	static void fabricator(RecipeOutput output, MKRecipeProvider recipes, @Nullable SizedFluidIngredient inputFluid, ItemLike result, int resultCount, Consumer<ShapedRecipeBuilder> pattern) {
		recipes.pushRecipeOutput(
			// the recipe is passed in by newOutput, letting us obtain the finished recipe instance from ModKit
			(id, recipe) -> output.accept(id("carpenter", MKRecipeProvider.path(result)), new FabricatorRecipe(Ingredient.EMPTY, Optional.ofNullable(inputFluid), (CraftingRecipe) recipe), null),
			// create a shaped recipe with the new output, which ModKit will pass into the above function
			newOutput -> recipes.shapedCrafting(RecipeCategory.MISC, result, resultCount, pattern)
		);
	}

	static void fabricatorSmelting(RecipeOutput output, String id, Ingredient input, FluidStack result, int meltingPoint) {
		output.accept(id("fabricator_smelting", id), new FabricatorSmeltingRecipe(input, result, meltingPoint), null);
	}
}
