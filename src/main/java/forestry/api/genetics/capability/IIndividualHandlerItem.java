package forestry.api.genetics.capability;

import forestry.api.ForestryCapabilities;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The individual handler manages an item's genetic information.
 * It contains the {@link IIndividual} and {@link ILifeStage} of the item.
 * This class can be thought of as the {@link IIndividual} analog of IFluidItemHandler.
 */
public interface IIndividualHandlerItem {
	/**
	 * @return The item containing this individual.
	 */
	ItemStack getContainer();

	/**
	 * @return The species type of this individual. Used for serialization/deserialization purposes, among other things.
	 */
	ISpeciesType<?, ?> getSpeciesType();

	/**
	 * @return The life stage of this individual
	 */
	ILifeStage getStage();

	/**
	 * @return The individual contained in this handler
	 */
	IIndividual getIndividual();

	/**
	 * @return {@code true} if this individual is the genetic form. Returns false for things like Vanilla saplings.
	 */
	boolean isGeneticForm();

	static void ifPresent(ItemStack stack, BiConsumer<IIndividual, ILifeStage> action) {
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
		if (handler != null) {
			action.accept(handler.getIndividual(), handler.getStage());
		}
	}

	static void ifPresent(ItemStack stack, Consumer<IIndividual> action) {
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
		if (handler != null) {
			action.accept(handler.getIndividual());
		}
	}

	/**
	 * @return Whether the given item has genetic data. (Vanilla saplings will return true too)
	 */
	static boolean isIndividual(ItemStack stack) {
		return stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM) != null;
	}

	/**
	 * Checks if the individual in this stack is present and if it matches some predicate.
	 *
	 * @param stack     The item to retrieve the individual from.
	 * @param predicate The predicate to test on the individual.
	 * @return {@code true} if the individual was present and the predicate returned true, {@code false} otherwise.
	 */
	static boolean filter(ItemStack stack, Predicate<IIndividual> predicate) {
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
		return handler != null && predicate.test(handler.getIndividual());
	}

	static boolean filter(ItemStack stack, BiPredicate<IIndividual, ILifeStage> predicate) {
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
		return handler != null && predicate.test(handler.getIndividual(), handler.getStage());
	}

	/**
	 * Retrieves the individual handler capability from the item stack if it is present.
	 *
	 * @param stack The item to get the individual handler from.
	 * @return The individual handler for this item, or null if none was found.
	 */
	@Nullable
	static IIndividualHandlerItem get(ItemStack stack) {
		return stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
	}

	@Nullable
	static IIndividual getIndividual(ItemStack stack) {
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null);
		return handler != null ? handler.getIndividual() : null;
	}

	/**
	 * Gets the species of the current item stack, or returns the default species for the species type.
	 */
	@SuppressWarnings("unchecked")
	static <S extends ISpecies<?>> S getSpecies(ItemStack stack, ISpeciesType<S, ?> type) {
		IIndividual individual = getIndividual(stack);
		return individual != null ? (S) individual.getSpecies() : type.getDefaultSpecies();
	}
}
