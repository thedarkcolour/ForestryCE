package forestry.api.client.genetics;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import forestry.api.core.IClimateSensitive;
import forestry.api.core.IProduct;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IIntegerChromosome;
import forestry.api.genetics.alleles.IValueChromosome;

public interface IAnalyzerGraphics<S extends ISpecies<I>, I extends IIndividual> {
	/**
	 * Draws a row starting with the name of the chromosome, then the active allele followed by the inactive allele.
	 *
	 * @param chromosome The chromosome to display.
	 */
	default <A extends IAllele> void drawChromosomeRow(IChromosome<A> chromosome) {
		drawChromosomeRow(chromosome, UnaryOperator.identity());
	}

	/**
	 * Draws a row starting with the name of the chromosome, then the active allele followed by the inactive allele.
	 *
	 * @param chromosome The chromosome to display.
	 * @param options    Further configuration of how the row is drawn and/or interacted with.
	 */
	<A extends IAllele> void drawChromosomeRow(IChromosome<A> chromosome, UnaryOperator<IChromosomeRow> options);

	/**
	 * Displays a table of the specimen's chromosomes. Automatically adds a species header with or without species icons.
	 * Supports haploid display as well, in which case the inactive column is omitted.
	 *
	 * @param stage  The stage used to render icons for the active/inactive species, or {@code null} for no icons.
	 */
	void drawSpeciesRow(@Nullable ILifeStage stage);

	/**
	 * Draws a row displaying information about the fertility chromosome of a specimen.
	 * It includes visual elements for the active allele value and an offspring sprite.
	 *
	 * @param chromosome     The fertility chromosome to display, containing alleles representing fertility values.
	 * @param offspringSprite The visual representation of the offspring associated with the chromosome.
	 */
	void drawFertilityRow(IIntegerChromosome chromosome, ResourceLocation offspringSprite);

	/**
	 * Draws temperature preference & tolerance in two rows, then humidity preference and tolerance in two more rows.
	 *
	 * @param temperatureTolerance The chromosome to use for temperature tolerance.
	 * @param humidityTolerance    The chromosome to use for humidity tolerance.
	 * @throws IllegalArgumentException If the table's genome is not for a {@link IClimateSensitive} species.
	 */
	void drawClimatePreferences(IValueChromosome<ToleranceType> temperatureTolerance, IValueChromosome<ToleranceType> humidityTolerance);

	/**
	 * Draws a list of active and inactive products for display in the analyzer graphics interface.
	 * Products that appear in both the active and inactive are not shown twice.
	 *
	 * @param getProducts Returns a list of products based on the species.
	 */
	void drawProductList(Function<S, List<IProduct>> getProducts);

	default void drawText(Component text) {
		drawText(text, TextAlign.LEFT);
	}

	default void drawText(Component text, int x) {
		drawText(text, TextAlign.LEFT, x);
	}

	default void drawText(Component text, TextAlign align) {
		drawText(text, align, 0);
	}

	void drawText(Component text, TextAlign align, int x);

	/**
	 * Adds an empty horizontal space by the specified number of pixels.
	 *
	 * @param x The number of pixels to add as spacing.
	 */
	void addHorizontalSpacing(int x);

	/**
	 * Adds an empty vertical space by the specified number of pixels.
	 *
	 * @param y The number of pixels to add as spacing.
	 */
	void addVerticalSpacing(int y);

	/**
	 * Determines whether the inactive alleles of the current genome should be shown.
	 *
	 * @param haploid If {@code true}, only the active alleles are shown.
	 */
	void setHaploid(boolean haploid);

	enum TextAlign {
		LEFT,
		CENTER,
		RIGHT,
	}

	interface IChromosomeRow {
		IChromosomeRow setHover();
	}
}
