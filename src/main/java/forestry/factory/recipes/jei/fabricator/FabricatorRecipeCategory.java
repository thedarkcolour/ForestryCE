package forestry.factory.recipes.jei.fabricator;

import forestry.api.ForestryConstants;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.compat.jei.JeiUtil;
import forestry.core.config.Constants;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.core.utils.RecipeUtil;
import forestry.factory.blocks.BlockFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryPlain;
import forestry.factory.features.FactoryBlocks;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.FabricatorSmeltingRecipe;
import forestry.modules.features.FeatureBlock;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nullable;
import java.util.*;

public class FabricatorRecipeCategory extends ForestryRecipeCategory<IFabricatorRecipe> {
	private final static ResourceLocation guiTexture = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/fabricator.png");
	private final IDrawable icon;
	@Nullable
	private final ICraftingGridHelper craftingGridHelper;

	public FabricatorRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(guiTexture, 20, 16, 136, 54), "block.forestry.fabricator");

		FeatureBlock<BlockFactoryPlain, BlockItem> fabricatorFeatureBlock = FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR);
		ItemStack fabricator = new ItemStack(fabricatorFeatureBlock.block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, fabricator);
		this.craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	private static Map<Fluid, List<FabricatorSmeltingRecipe>> getSmeltingInputs() {
		Map<Fluid, List<FabricatorSmeltingRecipe>> smeltingInputs = new HashMap<>();
		RecipeUtil.getRecipes(RecipeUtil.getRecipeManager(), FactoryRecipeTypes.FABRICATOR_SMELTING)
			.forEach(smelting -> {
				Fluid fluid = smelting.getResultFluid().getFluid();
				if (!smeltingInputs.containsKey(fluid)) {
					smeltingInputs.put(fluid, new ArrayList<>());
				}

				smeltingInputs.get(fluid).add(smelting);
			});

		return smeltingInputs;
	}

	@Override
	public RecipeType<IFabricatorRecipe> getRecipeType() {
		return ForestryRecipeType.FABRICATOR;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IFabricatorRecipe recipe, IFocusGroup focuses) {
		Optional<SizedFluidIngredient> recipeLiquid = recipe.getRequiredFluid();
		Fluid recipeFluid = recipeLiquid.getFluid();
		List<FabricatorSmeltingRecipe> smeltingRecipes = getSmeltingInputs().get(recipeFluid);
		List<ItemStack> smeltingInput = smeltingRecipes.stream()
			.flatMap(s -> Arrays.stream(s.input().getItems()))
			.toList();

		builder.addSlot(RecipeIngredientRole.INPUT, 6, 32)
			.setFluidRenderer(2000, false, 16, 16)
			.addIngredient(NeoForgeTypes.FLUID_STACK, recipeLiquid);

		ShapedRecipe craftingGridRecipe = recipe.getCraftingGridRecipe();
		List<IRecipeSlotBuilder> craftingSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.INPUT, 3, 3, 47, 1, 18);
		JeiUtil.setCraftingItems(craftingSlots, craftingGridRecipe, this.craftingGridHelper);

		// using Catalyst tells JEI that it is not actually part of the recipe
		builder.addSlot(RecipeIngredientRole.CATALYST, 6, 5)
			.addItemStacks(smeltingInput);

		// todo why is plan unused
		//Ingredient plan = recipe.getPlan();
		//builder.addSlot(RecipeIngredientRole.INPUT, 119, 1)
		//		.addIngredients(plan);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 119, 37)
			.addItemStack(craftingGridRecipe.getResultItem(RecipeUtil.getRegistryAccess()));
	}
}
