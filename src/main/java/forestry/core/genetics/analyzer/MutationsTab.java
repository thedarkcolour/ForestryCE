package forestry.core.genetics.analyzer;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.core.gui.GuiConstants;
import forestry.core.gui.elements.Alignment;
import forestry.core.gui.elements.DatabaseElement;
import forestry.core.gui.elements.GuiElementFactory;
import forestry.core.gui.elements.layouts.LayoutHelper;

public class MutationsTab<I extends IIndividual> extends DatabaseTab<I> {
	public MutationsTab(Supplier<ItemStack> stackSupplier) {
		super("mutations", stackSupplier);
	}

	@Override
	public void createElements(DatabaseElement container, IIndividual individual, ILifeStage stage, ItemStack stack) {
		ISpecies<?> species = individual.getSpecies();
		var speciesType = species.getType();
		var mutationContainer = speciesType.getMutations();

		Player player = Minecraft.getInstance().player;
		IBreedingTracker breedingTracker = speciesType.getBreedingTracker(player.level(), player.getGameProfile());

		LayoutHelper groupHelper = container.layoutHelper((x, y) -> GuiElementFactory.horizontal(16, 0, new Insets(0, 1, 0, 0)), 100, 16);
		Collection<? extends IMutation<?>> mutations = getValidMutations(mutationContainer.getMutationsFrom(species.cast()));

		if (!mutations.isEmpty()) {
			container.label(Component.translatable("for.gui.database.mutations.further"), Alignment.TOP_CENTER, GuiConstants.UNDERLINED_STYLE.withColor(ChatFormatting.WHITE));
			mutations.forEach(mutation -> groupHelper.add(GuiElementFactory.INSTANCE.createMutation(0, 0, 50, 16, mutation, species, breedingTracker)));
			groupHelper.finish(true);
		}

		Collection<? extends IMutation<?>> mutationsInto = getValidMutations(mutationContainer.getMutationsInto(species.cast()));
		if (mutationsInto.isEmpty()) {
			return;
		}
		container.label(Component.translatable("for.gui.database.mutations.resultant"), Alignment.TOP_CENTER, GuiConstants.UNDERLINED_STYLE.withColor(ChatFormatting.WHITE));
		mutationsInto.forEach(mutation -> groupHelper.add(GuiElementFactory.INSTANCE.createMutationResultant(0, 0, 50, 16, mutation, breedingTracker)));
		groupHelper.finish(true);
	}

	// filters the list for non-secret mutations
	private <M extends IMutation<?>> Collection<M> getValidMutations(List<M> mutations) {
		ArrayList<M> validMutations = new ArrayList<>();

		for (M mutation : mutations) {
			if (!mutation.isSecret()) {
				validMutations.add(mutation);
			}
		}

		return validMutations;
	}
}
