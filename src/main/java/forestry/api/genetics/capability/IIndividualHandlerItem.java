package forestry.api.genetics.capability;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;

import forestry.api.ForestryCapabilities;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;

/**
 * The individual handler manages an item's genetic information.
 * It contains the {@link IIndividual} and {@link ILifeStage} of the item.
 * This class can be thought of as the {@link IIndividual} analog of IFluidItemHandler.
 * In 1.21, this will be replaced by Components.
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
		if (!stack.isEmpty()) {
			stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null).ifPresent(handler -> action.accept(handler.getIndividual(), handler.getStage()));
		}
	}

	static void ifPresent(ItemStack stack, Consumer<IIndividual> action) {
		if (!stack.isEmpty()) {
			stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null).ifPresent(handler -> action.accept(handler.getIndividual()));
		}
	}

	/**
	 * @return Whether the given item has an individual capability. (Vanilla saplings have a capability too)
	 */
	static boolean isIndividual(ItemStack stack) {
		return !stack.isEmpty() && stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM).isPresent();
	}

	/**
	 * Checks if the individual in this stack is present and if it matches some predicate.
	 *
	 * @param stack     The item to retrieve the individual from.
	 * @param predicate The predicate to test on the individual.
	 * @return {@code true} if the individual was present and the predicate returned true, false otherwise.
	 */
	@SuppressWarnings({"ConstantValue", "DataFlowIssue"})
	static boolean filter(ItemStack stack, Predicate<IIndividual> predicate) {
		if (stack.isEmpty()) {
			return false;
		}
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null).orElse(null);
		return handler != null && predicate.test(handler.getIndividual());
	}

	@SuppressWarnings({"ConstantValue", "DataFlowIssue"})
	static boolean filter(ItemStack stack, BiPredicate<IIndividual, ILifeStage> predicate) {
		if (stack.isEmpty()) {
			return false;
		}
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null).orElse(null);
		return handler != null && predicate.test(handler.getIndividual(), handler.getStage());
	}

	/**
	 * Retrieves the individual handler capability from the item stack if it is present.
	 *
	 * @param stack The item to get the individual handler from.
	 * @return The individual handler for this item, or null if none was found.
	 */
	@Nullable
	@SuppressWarnings("DataFlowIssue")
	static IIndividualHandlerItem get(ItemStack stack) {
		return stack.isEmpty() ? null : stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null).orElse(null);
	}

	@Nullable
	@SuppressWarnings({"ConstantValue", "DataFlowIssue"})
	static IIndividual getIndividual(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		// hack fix for creative tabs
		stack.reviveCaps();
		IIndividualHandlerItem handler = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, null).orElse(null);
		return handler != null ? handler.getIndividual() : null;
	}

	/**
	 * Gets the species of the current item stack, or returns the default species for the species type.
	 */
	static <S extends ISpecies<?>> S getSpecies(ItemStack stack, ISpeciesType<S, ?> type) {
		IIndividual individual = getIndividual(stack);
		return individual != null ? (S) individual.getSpecies() : type.getDefaultSpecies();
	}
}
