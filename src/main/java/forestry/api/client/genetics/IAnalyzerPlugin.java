package forestry.api.client.genetics;

import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;

public interface IAnalyzerPlugin<S extends ISpecies<I>, I extends IIndividual> {
	/**
	 * The first page of the analyzer, typically used to display the most important chromosomes.
	 *
	 * @param graphics   The analyzer window where information is displayed.
	 * @param individual The currently analyzed individual, with genome data.
	 * @param stage      The life stage of the individual.
	 * @param specimen   The item form of the specimen currently in the slot.
	 */
	void drawPage1(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen);

	/**
	 * The second page of the analyzer, typically used to display the rest of the chromosomes.
	 *
	 * @param graphics   The analyzer window where information is displayed.
	 * @param individual The currently analyzed individual, with genome data.
	 * @param stage      The life stage of the individual.
	 * @param specimen   The item form of the specimen currently in the slot.
	 */
	void drawPage2(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen);

	void drawPage3(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen);

	default void drawPage4(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen) {
		// todo mutations
	}

	default void drawPage5(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen) {
		// todo taxonomy and flavor text
	}

	default <T extends IAnalyzerPlugin<?, ?>> T cast() {
		return (T) this;
	}
}
