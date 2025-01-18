package forestry.apiculture.compat;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.utils.JeiUtil;
import forestry.core.utils.ModUtil;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class MutationsRecipeCategory implements IRecipeCategory<MutationRecipe> {
	private static final int SPECIES_SLOTS_Y = 16;
	private static final int SPECIES_SLOT_0_X = 18 + 1;
	private static final int SPECIES_SLOT_1_X = 71 + 1;
	private static final int SPECIES_SLOT_2_X = 125 + 1;

	private final RecipeType<MutationRecipe> type;
	private final Component title;
	private final IDrawable background;
	private final IDrawable icon;
	final ISpeciesType<?, ?> speciesType;

	public MutationsRecipeCategory(ISpeciesType<?, ?> type, IDrawable background, IDrawable icon) {
		this.background = background;
		this.icon = icon;

		this.speciesType = type;
		this.type = new RecipeType<>(ModUtil.withSuffix(type.id(), "_mutations"), MutationRecipe.class);
		this.title = Component.translatable("for.jei.mutations." + type.id().getNamespace() + '.' + type.id().getPath());
	}

	@Override
	public RecipeType<MutationRecipe> getRecipeType() {
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
	public void setRecipe(IRecipeLayoutBuilder builder, MutationRecipe recipe, IFocusGroup focuses) {
		IIngredientAcceptor<?> inputs = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT);
		IIngredientAcceptor<?> outputs = builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT);
		IMutation<?> mutation = recipe.mutation;

		// special handling for the result individual who might have a special genome
		ItemStack result = recipe.result;
		IIndividualHandlerItem resultHandler = IIndividualHandlerItem.get(result);
		IGenome resultGenome = resultHandler == null ? mutation.getResult().getDefaultGenome() : resultHandler.getIndividual().getGenome();

		// makes all (default) members of the species show the recipe instead of just the drone or princess
		for (ILifeStage stage : mutation.getType().getLifeStages()) {
			inputs.addItemStack(mutation.getFirstParent().createStack(stage));
			inputs.addItemStack(mutation.getSecondParent().createStack(stage));
			outputs.addItemStack(mutation.getResult().createIndividual(resultGenome).createStack(stage));
		}

		// display correct stages in GUI
		builder.addSlot(RecipeIngredientRole.INPUT, SPECIES_SLOT_0_X, SPECIES_SLOTS_Y)
				.addItemStack(recipe.firstParent);
		builder.addSlot(RecipeIngredientRole.INPUT, SPECIES_SLOT_1_X, SPECIES_SLOTS_Y)
				.addItemStack(recipe.secondParent);
		builder.addSlot(RecipeIngredientRole.OUTPUT, SPECIES_SLOT_2_X, SPECIES_SLOTS_Y)
				.addItemStack(result);
	}

	static ItemStack createAnalyzedStack(ILifeStage stage, ISpecies<?> species, @Nullable IGenome genome) {
		if (genome == null) {
			genome = species.getDefaultGenome();
		}

		IIndividual individual = species.createIndividual(genome);
		individual.analyze();

		return individual.createStack(stage);
	}

	@Override
	public void draw(MutationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		JeiUtil.drawCenteredMulti(graphics, recipe.mutation.getFirstParent().getDisplayName(), SPECIES_SLOT_0_X + 9, SPECIES_SLOTS_Y + 22, 0xffffffff);
		JeiUtil.drawCenteredMulti(graphics, recipe.mutation.getSecondParent().getDisplayName(), SPECIES_SLOT_1_X + 9, SPECIES_SLOTS_Y + 22, 0xffffffff);
		JeiUtil.drawCenteredMulti(graphics, recipe.mutation.getResult().getDisplayName(), SPECIES_SLOT_2_X + 9, SPECIES_SLOTS_Y + 22, 0xffffffff);

		List<Component> conditions = recipe.mutation.getSpecialConditions();
		String percentageString = JeiUtil.formatPercentage(recipe.mutation.getChance()) + "%";

		if (conditions.isEmpty()) {
			JeiUtil.drawCenteredMulti(graphics, Component.literal(percentageString), 105, 12, 0xffffff);
		} else {
			JeiUtil.drawCenteredMulti(graphics, Component.literal('[' + percentageString + ']'), 105, 12, 0xffffff);
		}
	}

	@Override
	public List<Component> getTooltipStrings(MutationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		List<Component> mutation = recipe.mutation.getSpecialConditions();

		if (!mutation.isEmpty() && mouseX >= 90 && mouseX <= 120 && mouseY >= 11 && mouseY <= 19) {
			return mutation;
		} else {
			return List.of();
		}
	}
}
