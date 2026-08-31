package forestry.core.content.machines.recipes.jei.rainmaker;

import forestry.core.platform.client.ForestryColors;
import forestry.core.platform.recipes.jei.ForestryRecipeCategory;
import forestry.core.platform.recipes.jei.ForestryRecipeType;
import forestry.core.content.machines.blocks.BlockTypeFactoryTesr;
import forestry.core.content.machines.features.FactoryBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class RainmakerRecipeCategory extends ForestryRecipeCategory<RainmakerJeiRecipe> {
	private final IDrawable slot;
	private final IDrawable icon;

	public RainmakerRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createBlankDrawable(150, 30), "block.forestry.rainmaker");
		this.slot = guiHelper.getSlotDrawable();
		ItemStack rainmaker = new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.RAINMAKER).block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, rainmaker);
	}

	@Override
	public RecipeType<RainmakerJeiRecipe> getRecipeType() {
		return ForestryRecipeType.RAINMAKER;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RainmakerJeiRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
			.setBackground(this.slot, -1, -1)
			.addItemStack(recipe.substrate());
	}

	@Override
	public void draw(RainmakerJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
		Component effect = getEffectString(recipe);
		Component speed = Component.translatable("for.jei.rainmaker.speed", recipe.fuel().speed());

		Font font = Minecraft.getInstance().font;
		graphics.drawString(font, effect, 24, 0, ForestryColors.DARK_GRAY, false);
		graphics.drawString(font, speed, 24, 10, ForestryColors.GRAY, false);
		if (!recipe.fuel().reverse()) {
			Component duration = Component.translatable("for.jei.rainmaker.duration", recipe.fuel().duration());
			graphics.drawString(font, duration, 24, 20, ForestryColors.GRAY, false);
		}
	}

	private static Component getEffectString(RainmakerJeiRecipe recipe) {
		if (recipe.fuel().reverse()) {
			return Component.translatable("for.jei.rainmaker.stops.rain");
		} else {
			return Component.translatable("for.jei.rainmaker.causes.rain");
		}
	}
}
