package forestry.core.content.machines.recipes.jei.fabricator;

import forestry.api.ForestryConstants;
import forestry.api.core.machines.IFabricatorRecipe;
import forestry.api.core.machines.IFabricatorSmeltingRecipe;
import forestry.core.platform.config.Constants;
import forestry.core.platform.recipes.jei.ForestryRecipeCategory;
import forestry.core.platform.recipes.jei.ForestryRecipeType;
import forestry.core.platform.util.JeiUtil;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.blocks.FactoryBlock;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.features.FactoryRecipeTypes;
import forestry.core.platform.registration.FeatureBlock;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

public class FabricatorRecipeCategory extends ForestryRecipeCategory<IFabricatorRecipe> {
	private final static ResourceLocation guiTexture = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/thermionic_fabricator.png");
	private final IDrawable icon;
	@Nullable
	private final ICraftingGridHelper craftingGridHelper;

	public FabricatorRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(guiTexture, 20, 16, 136, 54), "block.forestry.thermionic_fabricator");

		FeatureBlock<FactoryBlock, BlockItem> fabricatorFeatureBlock = FactoryBlocks.PLAIN.get(FactoryBlockType.FABRICATOR);
		ItemStack fabricator = new ItemStack(fabricatorFeatureBlock.block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, fabricator);
		this.craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	private static Map<Fluid, List<IFabricatorSmeltingRecipe>> getSmeltingInputs() {
		Map<Fluid, List<IFabricatorSmeltingRecipe>> smeltingInputs = new HashMap<>();
		RecipeUtils.getRecipes(RecipeUtils.getRecipeManager(), FactoryRecipeTypes.FABRICATOR_SMELTING)
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
		FluidStack recipeLiquid = recipe.getResultFluid();
		Fluid recipeFluid = recipeLiquid.getFluid();
		List<IFabricatorSmeltingRecipe> smeltingRecipes = getSmeltingInputs().get(recipeFluid);
		List<ItemStack> smeltingInput = smeltingRecipes.stream()
			.flatMap(s -> Arrays.stream(s.getInput().getItems()))
			.toList();

		builder.addSlot(RecipeIngredientRole.INPUT, 6, 32)
			.setFluidRenderer(2000, false, 16, 16)
			.addFluidStack(recipeLiquid.getFluid(), recipeLiquid.getAmount());

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
			.addItemStack(craftingGridRecipe.getResultItem(RecipeUtils.getRegistryAccess()));
	}
}
