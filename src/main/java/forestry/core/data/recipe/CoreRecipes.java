package forestry.core.data.recipe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import forestry.api.ForestryConstants;
import forestry.factory.recipes.FabricatorSmeltingRecipe;
import forestry.factory.recipes.SqueezerRecipe;
import forestry.factory.recipes.StillRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import forestry.api.ForestryTags;
import forestry.api.circuits.ICircuit;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.blocks.EnumResourceType;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.circuits.ItemCircuitBoard;
import forestry.core.config.Constants;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.fluids.ForestryFluids;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.energy.blocks.EngineBlockType;
import forestry.energy.features.EnergyBlocks;
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
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import thedarkcolour.modkit.data.MKRecipeProvider;
import static thedarkcolour.modkit.data.MKRecipeProvider.ingredient;

public class CoreRecipes {
	public static ItemStack getContainer(EnumContainerType type, Fluid fluid) {
		ItemStack container = FluidsItems.CONTAINERS.stack(type);
		Optional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(container);
		return fluidHandlerCap.map(handler -> {
			handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
			return container;
		}).orElse(ItemStack.EMPTY);
	}

	public static void addRecipes(RecipeOutput consumer, MKRecipeProvider recipes) {
		// Vanilla recipe types
		ArboricultureRecipes.registerArboricultureRecipes(output, recipes);
		ApicultureRecipes.registerApicultureRecipes(recipes);
		ApicultureRecipes.registerFoodRecipes(recipes);
		StorageRecipes.registerBackpackRecipes(recipes);
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
		registerCarpenter(consumer, recipes);
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

	private static void registerFluidsRecipes(MKRecipeProvider recipes) {
		for (EnumContainerType containerType : EnumContainerType.values()) {
			recipes.shapedCrafting("cake_" + containerType.getSerializedName(), RecipeCategory.FOOD, Items.CAKE, recipe -> {
				recipe.define('A', StrictNBTIngredient.of(getContainer(containerType, NeoForgeMod.MILK.get())));
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
		Ingredient sealant = Ingredient.fromValues(Stream.of(new Ingredient.TagValue(ForestryTags.Items.PROPOLIS), new Ingredient.TagValue(Tags.Items.SLIMEBALLS)));
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
			recipe.define('W', StrictNBTIngredient.of(ItemCircuitBoard.createCircuitboard(EnumCircuitBoardType.REFINED, null, new ICircuit[]{})));
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
		carpenterRecipe(recipes, "impregnated_casing", 50, ForestryFluids.SEED_OIL.ingredient(250), Ingredient.EMPTY, );
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.SEED_OIL.ingredient(250))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.IMPREGNATED_CASING)
				.pattern("###")
				.pattern("# #")
				.pattern("###")
				.define('#', ItemTags.LOGS))
			.build(output, id("carpenter", "impregnated_casing"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.SEED_OIL.ingredient(500))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).item())
				.pattern("#  ")
				.pattern("###")
				.pattern("# #")
				.define('#', ItemTags.PLANKS))
			.build(output, id("carpenter", "escritoire"));
		new CarpenterRecipeBuilder()
			.setPackagingTime(50)
			.setLiquid(ForestryFluids.SEED_OIL.getFluid(100))
			.setBox(Ingredient.EMPTY)
			.recipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.IMPREGNATED_STICK), 2)
				.pattern("#")
				.pattern("#")
				.define('#', ItemTags.LOGS))
			.build(output, id("carpenter", "impregnated_stick"));
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

	private static void registerCentrifuge(RecipeOutput consumer) {
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
			.product(0.7f, honeyDrop)
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
			.setProcessingTime(20)
			.setInput(Ingredient.of(ApicultureItems.BEE_COMBS.get(EnumHoneyComb.VINTAGE)))
			.product(1.0f, CoreItems.BEESWAX.stack())
			.product(0.9f, ApicultureItems.HONEYDEW.stack())
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
	}

	private static void registerFabricatorSmelting(RecipeOutput consumer) {
		FluidStack liquidGlassBucket = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME);
		FluidStack liquidGlassX4 = ForestryFluids.GLASS.getFluid(FluidType.BUCKET_VOLUME * 4);
		FluidStack liquidGlass375 = ForestryFluids.GLASS.getFluid(375);

		fabricatorSmelting(consumer, "glass", Ingredient.of(Tags.Items.GLASS_BLOCKS_CHEAP), liquidGlassBucket, 1000);
		fabricatorSmelting(consumer, "glass_pane", Ingredient.of(Tags.Items.GLASS_PANES), liquidGlass375, 1000);
		fabricatorSmelting(consumer, "sand", Ingredient.of(Tags.Items.SANDS), liquidGlassBucket, 3000);
		fabricatorSmelting(consumer, "sandstone", Ingredient.of(Tags.Items.SANDSTONE_BLOCKS), liquidGlassX4, 4800);
	}

	private static void fabricatorSmelting(RecipeOutput output, String id, Ingredient input, FluidStack result, int meltingPoint) {
		output.accept(id("fabricator_smelting", id), new FabricatorSmeltingRecipe(input, result, meltingPoint), null);
	}

	private static void registerFermenter(RecipeOutput consumer) {
		// Apiculture
		new FermenterRecipeBuilder()
			.setResource(Ingredient.of(ApicultureItems.HONEYDEW))
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

	private static void registerSqueezer(RecipeOutput output) {
		FluidStack honeyDropFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP);
		FluidStack honeyBlockFluid = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP * 8);

		squeezerRecipe(output, "honey_drop", 10, List.of(Ingredient.of(ApicultureItems.HONEY_DROP)), honeyDropFluid, ApicultureItems.PROPOLIS.stack(EnumPropolis.NORMAL, 1), 5 / 100f);
		squeezerRecipe(output, "sponge_comb", 10, List.of(Ingredient.of(ApicultureItems.BEE_COMBS.stack(EnumHoneyComb.SPONGE))), honeyDropFluid, new ItemStack(Items.SPONGE), 2 / 100f);
		squeezerRecipe(output, "honey_block", 60, List.of(Ingredient.of(Items.HONEY_BLOCK)), honeyBlockFluid);
		squeezerRecipe(output, "honey_dew", 10, List.of(Ingredient.of(ApicultureItems.HONEYDEW)), honeyDropFluid);
		squeezerRecipe(output, "lava_sand", 20, List.of(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.VOLCANIC)), Ingredient.of(Items.SAND, Items.RED_SAND)), new FluidStack(Fluids.LAVA, 500));
		squeezerRecipe(output, "lava", 30, List.of(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.VOLCANIC)), Ingredient.of(Items.COBBLESTONE)), new FluidStack(Fluids.LAVA, 500));
		squeezerRecipe(output, "lava_magma", 20, List.of(Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.VOLCANIC)), Ingredient.of(Items.MAGMA_BLOCK)), new FluidStack(Fluids.LAVA, 1000));

		int seedOilAmount = 10;

		squeezerRecipe(output, "seeds", 10, List.of(Ingredient.of(Tags.Items.SEEDS)), ForestryFluids.SEED_OIL.getFluid(seedOilAmount));

		float mulchMultiplier = 0.2f;
		int juiceMultiplier = 200;

		squeezerRecipe(output, "mulch", 10, List.of(Ingredient.of(Items.APPLE, Items.CARROT)), ForestryFluids.JUICE.getFluid(juiceMultiplier), CoreItems.MULCH.stack(), mulchMultiplier);
		squeezerRecipe(output, "cactus", 10, List.of(Ingredient.of(Items.CACTUS)), new FluidStack(Fluids.WATER, 500));
		squeezerRecipe(output, "ice", 10, List.of(Ingredient.of(Items.SNOWBALL), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD)), Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.ICE_SHARD))), ForestryFluids.ICE.getFluid(4000));

		int seedOilMultiplier = 10;

		ItemStack mulch = new ItemStack(CoreItems.MULCH);
		Fluid seedOil = ForestryFluids.SEED_OIL.getFluid();
		Fluid juice = ForestryFluids.JUICE.getFluid();

		squeezerRecipe(output, "cherry", 20, List.of(Ingredient.of(ForestryTags.Items.CHERRY)), new FluidStack(seedOil, seedOilMultiplier * 5), mulch, 0.05F);
		squeezerRecipe(output, "walnut", 60, List.of(Ingredient.of(ForestryTags.Items.WALNUT)), new FluidStack(seedOil, seedOilMultiplier * 18), mulch, 0.05F);
		squeezerRecipe(output, "chestnut", 70, List.of(Ingredient.of(ForestryTags.Items.CHESTNUT)), new FluidStack(seedOil, seedOilMultiplier * 22), mulch, 0.02F);
		squeezerRecipe(output, "lemon", 10, List.of(Ingredient.of(ForestryTags.Items.LEMON)), new FluidStack(juice, juiceMultiplier * 2), mulch, mulchMultiplier / 2f);
		squeezerRecipe(output, "plum", 10, List.of(Ingredient.of(ForestryTags.Items.PLUM)), new FluidStack(juice, juiceMultiplier / 2), mulch, mulchMultiplier * 3f);
		squeezerRecipe(output, "papaya", 10, List.of(Ingredient.of(ForestryTags.Items.PAPAYA)), new FluidStack(juice, juiceMultiplier * 3), mulch, mulchMultiplier / 2f);
		squeezerRecipe(output, "dates", 10, List.of(Ingredient.of(ForestryTags.Items.DATE)), new FluidStack(juice, juiceMultiplier / 4), mulch, mulchMultiplier);
	}

	private static void squeezerRecipe(RecipeOutput output, String id, int processingTime, List<Ingredient> inputs, FluidStack result) {
		squeezerRecipe(output, id, processingTime, inputs, result, ItemStack.EMPTY, 0f);
	}

	private static void squeezerRecipe(RecipeOutput output, String id, int processingTime, List<Ingredient> inputs, FluidStack result, ItemStack remnants, float remnantsChance) {
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
}
