package forestry.core.content.machines.recipes.jei.smelter;

import forestry.api.ForestryConstants;
import forestry.api.core.machines.ISmelterRecipe;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.platform.config.Constants;
import forestry.core.platform.recipes.jei.ForestryRecipeCategory;
import forestry.core.platform.recipes.jei.ForestryRecipeType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Arrays;

public class SmelterRecipeCategory extends ForestryRecipeCategory<ISmelterRecipe> {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/smelter.png");

	private final IDrawableAnimated arrow;
	private final IDrawable icon;
	private final IDrawable fire;

	public SmelterRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(TEXTURE, 9, 16, 158, 62), "block.forestry.smelter");

		IDrawableStatic arrowDrawable = guiHelper.createDrawable(TEXTURE, 176, 52, 50, 16);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.LEFT, false);
		this.fire = guiHelper.createDrawable(TEXTURE, 176, 68, 14, 14);

		ItemStack smelter = new ItemStack(FactoryBlocks.PLAIN.get(FactoryBlockType.SMELTER).block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, smelter);
	}

	@Override
	public RecipeType<ISmelterRecipe> getRecipeType() {
		return ForestryRecipeType.SMELTER;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ISmelterRecipe recipe, IFocusGroup focuses) {
		int cellX = 0;
		int cellY = 0;

		int oX = 12;
		int oY = 5;

		for (SizedIngredient input : recipe.getInputs()) {
			// getItems already applies the count. Copy anyway, the array behind it is cached
			builder.addSlot(RecipeIngredientRole.INPUT, oX + cellX, oY + cellY).addItemStacks(
				Arrays.stream(input.getItems()).map(ItemStack::copy).toList()
			);

			cellX += 18;
			if (cellX >= 54) {
				cellX = 0;
				cellY += 18;
			}
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 23)
			.addItemStack(recipe.getOutput());
	}

	@Override
	public void draw(ISmelterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
		this.arrow.draw(graphics, 72, 23);
		this.fire.draw(graphics, 87, 42);
	}
}
