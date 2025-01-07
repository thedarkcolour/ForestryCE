package forestry.apiculture.compat;

import java.util.Comparator;
import java.util.LinkedList;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.core.utils.JeiUtil;
import forestry.core.utils.ModUtil;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

class ProductsRecipeCategory implements IRecipeCategory<ProductRecipe> {
	private static final int SPECIES_SLOT_X = 19 + 11;
	private static final int SPECIES_SLOT_Y = 16;
	private static final int PRODUCT_SLOTS_X = 93;
	private static final int PRODUCT_SLOTS_Y = 5;
	private static final int SPECIALTY_SLOTS_Y = 33;

	private final IDrawable icon;
	private final IDrawable background;
	private final RecipeType<ProductRecipe> type;
	private final Component title;
	final ISpeciesType<? extends ISpecies<?>, ?> speciesType;

	ProductsRecipeCategory(ISpeciesType<?, ?> type, IDrawable background, IDrawable icon) {
		this.background = background;
		this.icon = icon;

		this.speciesType = type;
		this.type = new RecipeType<>(ModUtil.withSuffix(type.id(), "_products"), ProductRecipe.class);
		this.title = Component.translatable("for.jei.products." + type.id().getNamespace() + '.' + type.id().getPath());
	}

	@Override
	public RecipeType<ProductRecipe> getRecipeType() {
		return this.type;
	}

	@Override
	public Component getTitle() {
		return this.title;
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ProductRecipe recipe, IFocusGroup focuses) {
		// for recipe lookup
		builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(recipe.inputs);

		// input (try to display queen or equivalent)
		builder.addSlot(RecipeIngredientRole.INPUT, SPECIES_SLOT_X, SPECIES_SLOT_Y)
				.addItemStack(MutationsRecipeCategory.createAnalyzedStack(speciesType.getTypeForMutation(2), recipe.species, null));

		// products
		if (recipe.products != null) {
			setProductsList(builder, recipe.products, PRODUCT_SLOTS_Y);
		}

		// specialties
		if (recipe.specialties != null) {
			setProductsList(builder, recipe.specialties, SPECIALTY_SLOTS_Y);
		}
	}

	private static void setProductsList(IRecipeLayoutBuilder builder, Object2FloatOpenCustomHashMap<ItemStack> productStacks, int slotsY) {
		IRecipeSlotTooltipCallback callback = (view, tooltip) -> view.getDisplayedItemStack().ifPresent(stack -> {
			if (productStacks.containsKey(stack)) {
				tooltip.add(JeiUtil.formatChance(productStacks.getFloat(stack)));
			}
		});

		int products = productStacks.size();

		// Sort the products by chance
		LinkedList<ItemStack> productsList = new LinkedList<>(productStacks.keySet());
		productsList.sort(Comparator.comparingDouble(productStacks).reversed());

		if (products <= 3) {
			for (int i = 0; i < products; i++) {
				builder.addSlot(RecipeIngredientRole.OUTPUT, ProductsRecipeCategory.PRODUCT_SLOTS_X + i * 22, slotsY)
						.addItemStack(productsList.get(i))
						.addTooltipCallback(callback);
			}
		} else {
			// Three copies of the product list, each offset by 0, 1, and 2, should look like a scrolling list in JEI
			for (int i = 0; i < 3; i++) {
				builder.addSlot(RecipeIngredientRole.OUTPUT, ProductsRecipeCategory.PRODUCT_SLOTS_X + i * 22, slotsY)
						.addItemStacks(productsList)
						.addTooltipCallback(callback);

				// rotate the list (no need to copy because JEI just iterates once)
				productsList.addLast(productsList.removeFirst());
			}
		}
	}

	@Override
	public void draw(ProductRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		JeiUtil.drawCenteredMulti(graphics, recipe.species.getDisplayName(), SPECIES_SLOT_X + 9, SPECIES_SLOT_Y + 22, 0xffffff);
	}
}
