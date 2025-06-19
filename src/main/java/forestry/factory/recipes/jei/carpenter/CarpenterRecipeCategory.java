package forestry.factory.recipes.jei.carpenter;

import forestry.api.ForestryConstants;
import forestry.api.recipes.ICarpenterRecipe;
import forestry.core.config.Constants;
import forestry.core.recipes.jei.ForestryRecipeCategory;
import forestry.core.recipes.jei.ForestryRecipeType;
import forestry.compat.jei.JeiUtil;
import forestry.core.utils.RecipeUtil;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CarpenterRecipeCategory extends ForestryRecipeCategory<ICarpenterRecipe> {
	private final static ResourceLocation guiTexture = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/carpenter.png");
	private final ICraftingGridHelper craftingGridHelper;
	private final IDrawableAnimated arrow;
	private final IDrawable tankOverlay;
	private final IDrawable icon;

	public CarpenterRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper.createDrawable(guiTexture, 9, 16, 158, 61), "block.forestry.carpenter");

        this.craftingGridHelper = guiHelper.createCraftingGridHelper();
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

		ItemStack processingIngredient = craftingGridRecipe.getResultItem(RecipeUtil.getRegistryAccess()).copy();
		processingIngredient.setCount(1);
		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 71, 35)
			.addItemStack(processingIngredient);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 120 - 9, 56 - 16)
			.addItemStack(craftingGridRecipe.getResultItem(RecipeUtil.getRegistryAccess()));

		List<IRecipeSlotBuilder> craftingSlots = JeiUtil.layoutSlotGrid(builder, RecipeIngredientRole.INPUT, 3, 3, 1, 4, 18);
		JeiUtil.setCraftingItems(craftingSlots, craftingGridRecipe, this.craftingGridHelper);

		// crate comes last to match the internal inventory layout of the carpenter
		builder.addSlot(RecipeIngredientRole.INPUT, 74, 4)
			.addIngredients(recipe.getBox());

		IRecipeSlotBuilder tankSlot = builder.addSlot(RecipeIngredientRole.INPUT, 141, 1)
			.setFluidRenderer(10000, false, 16, 58)
			.setOverlay(this.tankOverlay, 0, 0);

		Optional<SizedFluidIngredient> fluidResource = recipe.getInputFluid();
        fluidResource.ifPresent(sizedFluidIngredient -> tankSlot.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(sizedFluidIngredient.getFluids())));
	}

	@Override
	public void draw(ICarpenterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.arrow.draw(graphics, 89, 34);
	}
}
