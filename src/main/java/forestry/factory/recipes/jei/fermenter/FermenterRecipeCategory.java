package forestry.factory.recipes.jei.fermenter;

import forestry.api.ForestryConstants;
import forestry.api.ForestryDataMaps;
import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.IVariableFermentable;
import forestry.core.config.Constants;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

public class FermenterRecipeCategory extends ForestryRecipeCategory<IFermenterRecipe> {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/fermenter.png");

	private final IDrawableAnimated progressBar0;
	private final IDrawableAnimated progressBar1;
	private final IDrawable tankOverlay;
	private final IDrawable icon;
	private final List<ItemStack> fuels;

	public FermenterRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(TEXTURE, 34, 18, 108, 60), "block.forestry.fermenter");

		IDrawableStatic progressBarDrawable0 = guiHelper.createDrawable(TEXTURE, 176, 60, 4, 18);
		this.progressBar0 = guiHelper.createAnimatedDrawable(progressBarDrawable0, 40, IDrawableAnimated.StartDirection.BOTTOM, false);
		IDrawableStatic progressBarDrawable1 = guiHelper.createDrawable(TEXTURE, 176, 78, 4, 18);
		this.progressBar1 = guiHelper.createAnimatedDrawable(progressBarDrawable1, 80, IDrawableAnimated.StartDirection.BOTTOM, false);
		this.tankOverlay = guiHelper.createDrawable(TEXTURE, 192, 0, 16, 58);
		ItemStack fermenter = new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.FERMENTER).block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, fermenter);

		Registry<Item> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ITEM);
		this.fuels = registry.getDataMap(ForestryDataMaps.FERMENTER_FUELS).keySet().stream()
			.map(key -> new ItemStack(registry.getOrThrow(key)))
			.toList();
	}

	@Override
	public RecipeType<IFermenterRecipe> getRecipeType() {
		return ForestryRecipeType.FERMENTER;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IFermenterRecipe recipe, IFocusGroup focuses) {
		IRecipeSlotBuilder ingredientInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 51, 5)
			.addIngredients(recipe.getInputItem());

		builder.addSlot(RecipeIngredientRole.INPUT, 41, 39)
			.addItemStacks(this.fuels);

		int fermentationValue = recipe.getFermentationValue();
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
			.setFluidRenderer(3000, false, 16, 58)
			.setOverlay(this.tankOverlay, 0, 0)
			.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.stream(recipe.getInputFluid().getStacks())
				.map(stack -> stack.copyWithAmount(fermentationValue))
				.toList());

		final int baseAmount = Math.round(recipe.getFermentationValue() * recipe.getModifier());
		List<FluidStack> outputs =
			Arrays.stream(recipe.getInputItem().getItems())
				.map(fermentable -> {
					int amount = baseAmount;
					if (fermentable.getItem() instanceof IVariableFermentable variableFermentable) {
						amount *= variableFermentable.getFermentationModifier(fermentable);
					}
					return new FluidStack(recipe.getOutputFluid(), amount);
				})
				.toList();

		IRecipeSlotBuilder fluidOutputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 1)
			.setFluidRenderer(3000, false, 16, 58)
			.setOverlay(this.tankOverlay, 0, 0)
			.addIngredients(NeoForgeTypes.FLUID_STACK, outputs);

		builder.createFocusLink(ingredientInputSlot, fluidOutputSlot);
	}

	@Override
	public void draw(IFermenterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.progressBar0.draw(graphics, 40, 14);
		this.progressBar1.draw(graphics, 64, 28);
	}
}
