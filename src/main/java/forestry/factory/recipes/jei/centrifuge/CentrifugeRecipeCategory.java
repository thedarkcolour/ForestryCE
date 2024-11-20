package forestry.factory.recipes.jei.centrifuge;

import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.ForestryConstants;
import forestry.api.core.Product;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.core.config.Constants;
import forestry.core.recipes.jei.ChanceTooltipCallback;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.core.utils.JeiUtil;
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
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

public class CentrifugeRecipeCategory extends ForestryRecipeCategory<ICentrifugeRecipe> {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/centrifugesocket2.png");

	private final IDrawableAnimated arrow;
	private final IDrawable icon;

	public CentrifugeRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(TEXTURE, 11, 18, 154, 54), "block.forestry.centrifuge");

		IDrawableStatic arrowDrawable = guiHelper.createDrawable(TEXTURE, 176, 0, 4, 17);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 80, IDrawableAnimated.StartDirection.BOTTOM, false);
		ItemStack centrifuge = new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CENTRIFUGE).block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, centrifuge);
	}

	@Override
	public RecipeType<ICentrifugeRecipe> getRecipeType() {
		return ForestryRecipeType.CENTRIFUGE;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICentrifugeRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 19)
				.addIngredients(recipe.getInput());

		List<IRecipeSlotBuilder> outputSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.OUTPUT, 3, 3, 101, 1, 18);

		List<Product> sortedProducts = recipe.getAllProducts().stream()
				.sorted(Comparator.comparing(Product::chance).reversed())
				.toList();
		for (int i = 0; i < sortedProducts.size() && i < outputSlots.size(); i++) {
			Product product = sortedProducts.get(i);
			outputSlots.get(i)
					.addItemStack(product.createStack())
					.addTooltipCallback(new ChanceTooltipCallback(product.chance()));
		}
	}

	@Override
	public void draw(ICentrifugeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.arrow.draw(graphics, 32, 18);
		this.arrow.draw(graphics, 56, 18);
	}
}
