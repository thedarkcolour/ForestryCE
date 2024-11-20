package forestry.factory.recipes.jei.moistener;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.ForestryConstants;
import forestry.api.fuels.FuelManager;
import forestry.api.fuels.MoistenerFuel;
import forestry.api.recipes.IMoistenerRecipe;
import forestry.core.config.Constants;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

public class MoistenerRecipeCategory extends ForestryRecipeCategory<IMoistenerRecipe> {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/moistener.png");

	private final IDrawableAnimated arrow;
	private final IDrawableAnimated progressBar;
	private final IDrawable tankOverlay;
	private final IDrawable icon;
	private final List<ItemStack> fuelResources;
	private final List<ItemStack> fuelProducts;

	public MoistenerRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(TEXTURE, 15, 15, 145, 60), "block.forestry.moistener");

		IDrawableStatic arrowDrawable = guiHelper.createDrawable(TEXTURE, 176, 91, 29, 55);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 80, IDrawableAnimated.StartDirection.BOTTOM, false);
		IDrawableStatic progressBar = guiHelper.createDrawable(TEXTURE, 176, 74, 16, 15);
		this.progressBar = guiHelper.createAnimatedDrawable(progressBar, 160, IDrawableAnimated.StartDirection.LEFT, false);
		this.tankOverlay = guiHelper.createDrawable(TEXTURE, 176, 0, 16, 58);
		ItemStack moistener = new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.MOISTENER).block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, moistener);

		Collection<MoistenerFuel> fuels = FuelManager.moistenerResource.values();
		fuelResources = fuels.stream()
				.map(MoistenerFuel::resource)
				.toList();
		fuelProducts = fuels.stream()
				.map(MoistenerFuel::product)
				.toList();
	}

	@Override
	public RecipeType<IMoistenerRecipe> getRecipeType() {
		return ForestryRecipeType.MOISTENER;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IMoistenerRecipe recipe, IFocusGroup focuses) {


		builder.addSlot(RecipeIngredientRole.INPUT, 128, 4)
				.addIngredients(recipe.getInput());

		IRecipeSlotBuilder fuelResourceSlot = builder.addSlot(RecipeIngredientRole.INPUT, 24, 43)
				.addItemStacks(fuelResources);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 40)
				.addItemStack(recipe.getProduct());

		IRecipeSlotBuilder fuelProductsSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 22)
				.addItemStacks(fuelProducts);

		FluidStack fluidInput = new FluidStack(Fluids.WATER, recipe.getTimePerItem() / 4);
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
				.setFluidRenderer(10000, false, 16, 58)
				.setOverlay(tankOverlay, 0, 0)
				.addIngredient(ForgeTypes.FLUID_STACK, fluidInput);

		builder.createFocusLink(fuelResourceSlot, fuelProductsSlot);
	}

	@Override
	public void draw(IMoistenerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.arrow.draw(graphics, 78, 2);
		this.progressBar.draw(graphics, 109, 22);
	}
}
