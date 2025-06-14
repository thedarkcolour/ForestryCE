package forestry.core.data.recipe;

import forestry.api.ForestryTags;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.items.definitions.EnumContainerType;
import forestry.factory.blocks.BlockTypeFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
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
}
