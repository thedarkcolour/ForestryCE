package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.apiculture.blocks.BlockTypeApiculture;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.EnumElectronTube;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import thedarkcolour.modkit.data.MKRecipeProvider;

public class ApicultureRecipes {
	static void registerApicultureRecipes(MKRecipeProvider recipes) {
		registerCombRecipes(recipes);

		// Bee housings
		beeHousings(recipes);

		// Apiarist Suit
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

		recipes.shapedCrafting(RecipeCategory.MISC, CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST), recipe -> {
			recipe.define('G', Tags.Items.GLASS_BLOCKS);
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
			recipe.define('L', Tags.Items.LEATHERS);
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

		recipes.shapelessCrafting("exp_bottle_from_exp_drop", RecipeCategory.MISC, Items.EXPERIENCE_BOTTLE, 1, Items.GLASS_BOTTLE, ApicultureItems.EXPERIENCE_DROP.item());
	}

	private static void registerCombRecipes(MKRecipeProvider recipes) {
		for (EnumHoneyComb honeyComb : EnumHoneyComb.values()) {
			ItemLike comb = ApicultureItems.BEE_COMBS.get(honeyComb);
			Block combBlock = ApicultureBlocks.BEE_COMB.get(honeyComb).block();
			recipes.grid2x2(RecipeCategory.BUILDING_BLOCKS, combBlock, 1, Ingredient.of(comb), "combs");
		}
	}

	private static void beeHousings(MKRecipeProvider recipes) {
		// Bee House
		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureBlocks.BASE.get(BlockTypeApiculture.BEE_HOUSE).block(), recipe -> {
			recipe.define('S', ItemTags.WOODEN_SLABS);
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('C', ForestryTags.Items.BEE_COMBS);
			recipe.pattern("SSS");
			recipe.pattern("PCP");
			recipe.pattern("PPP");
		});
		// Apiary
		recipes.shapedCrafting(RecipeCategory.MISC, ApicultureBlocks.BASE.get(BlockTypeApiculture.APIARY).block(), recipe -> {
			recipe.define('S', ItemTags.WOODEN_SLABS);
			recipe.define('P', ItemTags.PLANKS);
			recipe.define('C', CoreItems.IMPREGNATED_CASING);
			recipe.pattern("SSS");
			recipe.pattern("PCP");
			recipe.pattern("PPP");
		});

		// Alveary components
		BlockAlveary plain = ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.PLAIN).block();
		ItemLike goldElectronTube = CoreItems.ELECTRON_TUBES.get(EnumElectronTube.GOLD);

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, plain, recipe -> {
			recipe.define('X', CoreItems.IMPREGNATED_CASING);
			recipe.define('#', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.SCENTED_PANELING));
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.FAN).block(), recipe -> {
			recipe.define('#', goldElectronTube);
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("I I");
			recipe.pattern(" X ");
			recipe.pattern("I#I");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.HEATER).block(), recipe -> {
			recipe.define('#', goldElectronTube);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.define('X', plain);
			recipe.define('S', Tags.Items.STONES);
			recipe.pattern("#I#");
			recipe.pattern(" X ");
			recipe.pattern("SSS");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.HYGRO).block(), recipe -> {
			recipe.define('G', Tags.Items.GLASS_BLOCKS);
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("GIG");
			recipe.pattern("GXG");
			recipe.pattern("GIG");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.SIEVE).block(), recipe -> {
			recipe.define('W', CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK));
			recipe.define('X', plain);
			recipe.define('I', Tags.Items.INGOTS_IRON);
			recipe.pattern("III");
			recipe.pattern(" X ");
			recipe.pattern("WWW");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.STABILISER).block(), recipe -> {
			recipe.define('X', plain);
			recipe.define('G', Tags.Items.GEMS_QUARTZ);
			recipe.pattern("G G");
			recipe.pattern("GXG");
			recipe.pattern("G G");
			recipe.group("alveary");
		});

		recipes.shapedCrafting(RecipeCategory.BUILDING_BLOCKS, ApicultureBlocks.ALVEARY.get(BlockAlveary.Type.SWARMER).block(), recipe -> {
			recipe.define('#', CoreItems.ELECTRON_TUBES.get(EnumElectronTube.DIAMOND));
			recipe.define('X', plain);
			recipe.define('G', Tags.Items.INGOTS_GOLD);
			recipe.pattern("#G#");
			recipe.pattern(" X ");
			recipe.pattern("#G#");
			recipe.group("alveary");
		});
	}

	static void registerFoodRecipes(MKRecipeProvider recipes) {
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

		recipes.shapedCrafting(RecipeCategory.FOOD, ApicultureItems.HONEYED_SLICE, recipe -> {
			recipe.define('#', honeyDrop);
			recipe.define('X', Items.BREAD);
			recipe.pattern("###");
			recipe.pattern("#X#");
			recipe.pattern("###");
		});

		recipes.shapelessCrafting("bottled_honey_drops", RecipeCategory.FOOD, Items.HONEY_BOTTLE, 1, Items.GLASS_BOTTLE, honeyDrop, honeyDrop);

	}
}
