package forestry.factory.recipes.jei.carpenter;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.ForestryConstants;
import forestry.api.recipes.ICarpenterRecipe;
import forestry.core.config.Constants;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.core.utils.JeiUtil;
import forestry.core.utils.RecipeUtils;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

public class CarpenterRecipeCategory extends ForestryRecipeCategory<ICarpenterRecipe> {
	private final static ResourceLocation guiTexture = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/carpenter.png");
	private final ICraftingGridHelper craftingGridHelper;
	private final IDrawableAnimated arrow;
	private final IDrawable tankOverlay;
	private final IDrawable icon;

	public CarpenterRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(guiTexture, 9, 16, 158, 61), "block.forestry.carpenter");

		craftingGridHelper = guiHelper.createCraftingGridHelper();
		IDrawableStatic arrowDrawable = guiHelper.createDrawable(guiTexture, 176, 59, 4, 17);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.BOTTOM, false);
		this.tankOverlay = guiHelper.createDrawable(guiTexture, 176, 0, 16, 58);
		ItemStack carpenter = new ItemStack(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER).block());
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, carpenter);
	}

	@Override
	public RecipeType<ICarpenterRecipe> getRecipeType() {
		return ForestryRecipeType.CARPENTER;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICarpenterRecipe recipe, IFocusGroup focuses) {
		CraftingRecipe craftingGridRecipe = recipe.getCraftingGridRecipe();

		ItemStack processingIngredient = craftingGridRecipe.getResultItem(RecipeUtils.getRegistryAccess()).copy();
		processingIngredient.setCount(1);
		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 71, 35)
				.addItemStack(processingIngredient);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 120 - 9, 56 - 16)
				.addItemStack(craftingGridRecipe.getResultItem(RecipeUtils.getRegistryAccess()));

		List<IRecipeSlotBuilder> craftingSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.INPUT, 3, 3, 1, 4, 18);
		JeiUtil.setCraftingItems(craftingSlots, craftingGridRecipe, craftingGridHelper);

		// crate comes last to match the internal inventory layout of the carpenter
		builder.addSlot(RecipeIngredientRole.INPUT, 74, 4)
				.addIngredients(recipe.getBox());

		IRecipeSlotBuilder tankSlot = builder.addSlot(RecipeIngredientRole.INPUT, 141, 1)
				.setFluidRenderer(10000, false, 16, 58)
				.setOverlay(tankOverlay, 0, 0);

		FluidStack fluidResource = recipe.getInputFluid();
		if (!fluidResource.isEmpty()) {
			tankSlot.addIngredient(ForgeTypes.FLUID_STACK, fluidResource);
		}
	}

	@Override
	public void draw(ICarpenterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.arrow.draw(graphics, 89, 34);
	}
}
