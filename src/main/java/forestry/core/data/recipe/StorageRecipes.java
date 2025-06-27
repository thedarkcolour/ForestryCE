package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.config.Constants;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.utils.ModUtil;
import forestry.modules.features.FeatureItem;
import forestry.storage.features.BackpackItems;
import forestry.storage.features.CrateItems;
import forestry.storage.items.ItemCrated;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import thedarkcolour.modkit.data.MKRecipeProvider;

public class StorageRecipes {
	static void registerBackpackRecipes(RecipeOutput output, MKRecipeProvider recipes) {
		recipes.shapedCrafting(RecipeCategory.TOOLS, BackpackItems.ADVENTURER_BACKPACK, recipe -> {
			recipe.define('#', ItemTags.WOOL);
			recipe.define('V', Tags.Items.BONES);
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

		wovenBackpack(output, "miner", BackpackItems.MINER_BACKPACK, BackpackItems.MINER_BACKPACK_T_2);
		wovenBackpack(output, "digger", BackpackItems.DIGGER_BACKPACK, BackpackItems.DIGGER_BACKPACK_T_2);
		wovenBackpack(output, "forester", BackpackItems.FORESTER_BACKPACK, BackpackItems.FORESTER_BACKPACK_T_2);
		wovenBackpack(output, "hunter", BackpackItems.HUNTER_BACKPACK, BackpackItems.HUNTER_BACKPACK_T_2);
		wovenBackpack(output, "adventurer", BackpackItems.ADVENTURER_BACKPACK, BackpackItems.ADVENTURER_BACKPACK_T_2);
		wovenBackpack(output, "builder", BackpackItems.BUILDER_BACKPACK, BackpackItems.BUILDER_BACKPACK_T_2);

		// Naturalist backpacks
		naturalistBackpack(recipes, BackpackItems.APIARIST_BACKPACK, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST));
		naturalistBackpack(recipes, BackpackItems.LEPIDOPTERIST_BACKPACK, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST));
		naturalistBackpack(recipes, BackpackItems.ARBORIST_BACKPACK, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST));

		crate(output, CrateItems.CRATED_PEAT.get(), Ingredient.of(CoreItems.PEAT));
		crate(output, CrateItems.CRATED_APATITE.get(), Ingredient.of(ForestryTags.Items.GEMS_APATITE));
		crate(output, CrateItems.CRATED_FERTILIZER_COMPOUND.get(), Ingredient.of(CoreItems.FERTILIZER_COMPOUND));
		crate(output, CrateItems.CRATED_MULCH.get(), Ingredient.of(CoreItems.MULCH));
		crate(output, CrateItems.CRATED_PHOSPHOR.get(), Ingredient.of(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.PHOSPHOR)));
		crate(output, CrateItems.CRATED_ASH.get(), Ingredient.of(CoreItems.ASH));
		crate(output, CrateItems.CRATED_TIN.get(), Ingredient.of(ForestryTags.Items.INGOTS_TIN));
		crate(output, CrateItems.CRATED_COPPER.get(), Ingredient.of(Items.COPPER_INGOT));
		crate(output, CrateItems.CRATED_BRONZE.get(), Ingredient.of(ForestryTags.Items.INGOTS_BRONZE));

		crate(output, CrateItems.CRATED_HUMUS.get(), Ingredient.of(CoreBlocks.HUMUS));
		crate(output, CrateItems.CRATED_BOG_EARTH.get(), Ingredient.of(CoreBlocks.BOG_EARTH));

		crate(output, CrateItems.CRATED_WHEAT.get(), Ingredient.of(Tags.Items.CROPS_WHEAT));
		crate(output, CrateItems.CRATED_COOKIE.get(), Ingredient.of(Items.COOKIE));
		crate(output, CrateItems.CRATED_REDSTONE.get(), Ingredient.of(Tags.Items.DUSTS_REDSTONE));
		crate(output, CrateItems.CRATED_LAPIS.get(), Ingredient.of(Tags.Items.GEMS_LAPIS));
		crate(output, CrateItems.CRATED_SUGAR_CANE.get(), Ingredient.of(Items.SUGAR_CANE));
		crate(output, CrateItems.CRATED_CLAY_BALL.get(), Ingredient.of(Items.CLAY_BALL));
		crate(output, CrateItems.CRATED_GLOWSTONE.get(), Ingredient.of(Tags.Items.DUSTS_GLOWSTONE));
		crate(output, CrateItems.CRATED_APPLE.get(), Ingredient.of(Items.APPLE));
		crate(output, CrateItems.CRATED_COAL.get(), Ingredient.of(Items.COAL));
		crate(output, CrateItems.CRATED_CHARCOAL.get(), Ingredient.of(Items.CHARCOAL));
		crate(output, CrateItems.CRATED_SEEDS.get(), Ingredient.of(Items.WHEAT_SEEDS));
		crate(output, CrateItems.CRATED_POTATO.get(), Ingredient.of(Tags.Items.CROPS_POTATO));
		crate(output, CrateItems.CRATED_CARROT.get(), Ingredient.of(Tags.Items.CROPS_CARROT));
		crate(output, CrateItems.CRATED_BEETROOT.get(), Ingredient.of(Tags.Items.CROPS_BEETROOT));
		crate(output, CrateItems.CRATED_NETHER_WART.get(), Ingredient.of(Tags.Items.CROPS_NETHER_WART));

		crate(output, CrateItems.CRATED_OAK_LOG.get(), Ingredient.of(Items.OAK_LOG));
		crate(output, CrateItems.CRATED_BIRCH_LOG.get(), Ingredient.of(Items.BIRCH_LOG));
		crate(output, CrateItems.CRATED_JUNGLE_LOG.get(), Ingredient.of(Items.JUNGLE_LOG));
		crate(output, CrateItems.CRATED_SPRUCE_LOG.get(), Ingredient.of(Items.SPRUCE_LOG));
		crate(output, CrateItems.CRATED_ACACIA_LOG.get(), Ingredient.of(Items.ACACIA_LOG));
		crate(output, CrateItems.CRATED_DARK_OAK_LOG.get(), Ingredient.of(Items.DARK_OAK_LOG));
		crate(output, CrateItems.CRATED_COBBLESTONE.get(), Ingredient.of(Tags.Items.COBBLESTONES));
		crate(output, CrateItems.CRATED_DIRT.get(), Ingredient.of(Items.DIRT));
		crate(output, CrateItems.CRATED_GRASS_BLOCK.get(), Ingredient.of(Items.GRASS_BLOCK));
		crate(output, CrateItems.CRATED_STONE.get(), Ingredient.of(Tags.Items.STONES));
		crate(output, CrateItems.CRATED_GRANITE.get(), Ingredient.of(Items.GRANITE));
		crate(output, CrateItems.CRATED_DIORITE.get(), Ingredient.of(Items.DIORITE));
		crate(output, CrateItems.CRATED_ANDESITE.get(), Ingredient.of(Items.ANDESITE));
		crate(output, CrateItems.CRATED_PRISMARINE.get(), Ingredient.of(Items.PRISMARINE));
		crate(output, CrateItems.CRATED_PRISMARINE_BRICKS.get(), Ingredient.of(Items.PRISMARINE_BRICKS));
		crate(output, CrateItems.CRATED_DARK_PRISMARINE.get(), Ingredient.of(Items.DARK_PRISMARINE));
		crate(output, CrateItems.CRATED_BRICKS.get(), Ingredient.of(Items.BRICKS));
		crate(output, CrateItems.CRATED_CACTUS.get(), Ingredient.of(Items.CACTUS));
		crate(output, CrateItems.CRATED_SAND.get(), Ingredient.of(Items.SAND));
		crate(output, CrateItems.CRATED_RED_SAND.get(), Ingredient.of(Items.RED_SAND));
		crate(output, CrateItems.CRATED_OBSIDIAN.get(), Ingredient.of(Tags.Items.OBSIDIANS));
		crate(output, CrateItems.CRATED_NETHERRACK.get(), Ingredient.of(Tags.Items.NETHERRACKS));
		crate(output, CrateItems.CRATED_SOUL_SAND.get(), Ingredient.of(Items.SOUL_SAND));
		crate(output, CrateItems.CRATED_SANDSTONE.get(), Ingredient.of(Tags.Items.SANDSTONE_BLOCKS));
		crate(output, CrateItems.CRATED_NETHER_BRICKS.get(), Ingredient.of(Items.NETHER_BRICKS));
		crate(output, CrateItems.CRATED_MYCELIUM.get(), Ingredient.of(Items.MYCELIUM));
		crate(output, CrateItems.CRATED_GRAVEL.get(), Ingredient.of(Tags.Items.GRAVELS));
		crate(output, CrateItems.CRATED_OAK_SAPLING.get(), Ingredient.of(Items.OAK_SAPLING));
		crate(output, CrateItems.CRATED_BIRCH_SAPLING.get(), Ingredient.of(Items.BIRCH_SAPLING));
		crate(output, CrateItems.CRATED_JUNGLE_SAPLING.get(), Ingredient.of(Items.JUNGLE_SAPLING));
		crate(output, CrateItems.CRATED_SPRUCE_SAPLING.get(), Ingredient.of(Items.SPRUCE_SAPLING));
		crate(output, CrateItems.CRATED_ACACIA_SAPLING.get(), Ingredient.of(Items.ACACIA_SAPLING));
		crate(output, CrateItems.CRATED_DARK_OAK_SAPLING.get(), Ingredient.of(Items.DARK_OAK_SAPLING));

		crate(output, CrateItems.CRATED_BEESWAX.get(), Ingredient.of(CoreItems.BEESWAX));
		crate(output, CrateItems.CRATED_REFRACTORY_WAX.get(), Ingredient.of(CoreItems.REFRACTORY_WAX));

		crate(output, CrateItems.CRATED_POLLEN_CLUSTER_NORMAL.get(), Ingredient.of(ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.NORMAL)));
		crate(output, CrateItems.CRATED_POLLEN_CLUSTER_CRYSTALLINE.get(), Ingredient.of(ApicultureItems.POLLEN_CLUSTER.get(EnumPollenCluster.CRYSTALLINE)));
		crate(output, CrateItems.CRATED_PROPOLIS.get(), Ingredient.of(ApicultureItems.PROPOLIS.get(EnumPropolis.NORMAL)));
		crate(output, CrateItems.CRATED_HONEYDEW.get(), Ingredient.of(ApicultureItems.HONEYDEW));
		crate(output, CrateItems.CRATED_ROYAL_JELLY.get(), Ingredient.of(ApicultureItems.ROYAL_JELLY));

		for (EnumHoneyComb comb : EnumHoneyComb.values()) {
			crate(output, CrateItems.CRATED_BEE_COMBS.get(comb).get(), Ingredient.of(ApicultureItems.BEE_COMBS.get(comb)));
		}
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

	static void wovenBackpack(RecipeOutput consumer, MKRecipeProvider recipes, String id, FeatureItem<?> tier1, FeatureItem<?> tier2) {
		CoreRecipes.carpenter(consumer, recipes, 200, SizedFluidIngredient.of(Fluids.WATER, 1000), Ingredient.EMPTY, tier2, 1, recipe -> {
			recipe.pattern("WXW");
			recipe.pattern("WTW");
			recipe.pattern("WWW");
			recipe.define('W', CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.WOVEN_SILK).getItem());
			recipe.define('X', Items.DIAMOND);
			recipe.define('T', tier1);
		});
	}

	static void crate(RecipeOutput consumer, ItemCrated crated, Ingredient ingredient) {
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
			.build(consumer, CoreRecipes.id("carpenter", "crates", "pack", name.getNamespace(), name.getPath()));
		new CarpenterRecipeBuilder()
			.setPackagingTime(Constants.CARPENTER_UNCRATING_CYCLES)
			.setBox(Ingredient.EMPTY)
			.recipe(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, contained.getItem(), 9).requires(crated))
			.build(consumer, CoreRecipes.id("carpenter", "crates", "unpack", name.getNamespace(), name.getPath()));
	}
}
