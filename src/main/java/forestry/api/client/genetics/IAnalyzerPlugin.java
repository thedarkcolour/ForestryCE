package forestry.api.client.genetics;

import forestry.api.genetics.*;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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

	/**
	 * The third page of the analyzer, typically used to display produce.
	 *
	 * @param graphics   The analyzer window where information is displayed.
	 * @param individual The currently analyzed individual, with genome data.
	 * @param stage      The life stage of the individual.
	 * @param specimen   The item form of the specimen currently in the slot.
	 */
	void drawPage3(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen);

	/**
	 * The fourth page of the analyzer, usually just the mutations this individual's active species is used in.
	 * To use Forestry's built-in mutations page, call {@link IAnalyzerGraphics#drawMutationsPage}.
	 *
	 * @param graphics   The analyzer window where information is displayed.
	 * @param individual The currently analyzed individual, with genome data.
	 * @param stage      The life stage of the individual.
	 * @param specimen   The item form of the specimen currently in the slot.
	 */
	void drawPage4(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen);

	/**
	 * The fifth page of the analyzer, usually used to display the individual's taxonomy and flavor text.
	 * To use Forestry's built-in taxonomy page, call {@link IAnalyzerGraphics#drawTaxonomyPage}.
	 *
	 * @param graphics   The analyzer window where information is displayed.
	 * @param individual The currently analyzed individual, with genome data.
	 * @param stage      The life stage of the individual.
	 * @param specimen   The item form of the specimen currently in the slot.
	 */
	default void drawPage5(IAnalyzerGraphics<S, I> graphics, I individual, ILifeStage stage, ItemStack specimen) {
		graphics.drawTaxonomyPage();
	}

	default List<String> getHints() {
		return List.of();
	}
}
