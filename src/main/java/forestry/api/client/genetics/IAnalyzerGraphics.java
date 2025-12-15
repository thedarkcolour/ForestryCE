package forestry.api.client.genetics;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.client.InteractableTextOptions;
import forestry.api.client.TextOptions;
import forestry.api.core.IClimateSensitive;
import forestry.api.core.IProduct;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.IIndividual;
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
	default <C extends IChromosome<A>, A extends IAllele> void drawChromosomeRow(C chromosome) {
		drawChromosomeRow(chromosome, null);
	}

	/**
	 * Draws a row starting with the name of the chromosome, then the active allele followed by the inactive allele.
	 *
	 * @param chromosome The chromosome to display.
	 * @param options    Further configuration of how the row is drawn and/or interacted with.
	 */
	<C extends IChromosome<A>, A extends IAllele> void drawChromosomeRow(C chromosome, @Nullable IChromosomeRowOptions<C, A> options);

	/**
	 * Displays a table of the specimen's chromosomes. Automatically adds a species header with or without species icons.
	 * Supports haploid display as well, in which case the inactive column is omitted.
	 *
	 * @param iconGetter The function used to map species to item-s icons for the active/inactive species, or {@code null} for no icons.
	 */
	void drawSpeciesIconsRow(@Nullable Function<S, ItemStack> iconGetter);

	/**
	 * Draws a row displaying information about the fertility chromosome of a specimen.
	 * It includes visual elements for the active allele value and an offspring sprite.
	 *
	 * @param chromosome      The fertility chromosome to display, containing alleles representing fertility values.
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
		drawText(text, 0);
	}

	default void drawText(Component text, int x) {
		drawText(text, x, null);
	}

	void drawText(Component text, int x, @Nullable InteractableTextOptions options);

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
	 * Adds an empty vertical space by the specified number of lines.
	 * Similar to {@link #addVerticalSpacing(int)}, but uses font lines as a unit instead of pixels.
	 *
	 * @param lines The number of lines to shift down by. Line height is usually 12 pixels.
	 */
	void addLineSpacing(int lines);

	/**
	 * Determines whether the inactive alleles of the current genome should be shown.
	 *
	 * @param haploid If {@code true}, only the active alleles are shown.
	 */
	void setHaploid(boolean haploid);

	default void drawTooltip(int x, int y, Component tooltip) {
		drawTooltip(x, y, tooltip, null);
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @param tooltip
	 * @param options Text styling options. Note that "on hover" and "on click" behaviors aren't supported.
	 */
	default void drawTooltip(int x, int y, Component tooltip, @Nullable TextOptions options) {
		drawTooltip(x, y, List.of(tooltip), options);
	}

	void drawTooltip(int x, int y, List<Component> tooltip, @Nullable TextOptions options);

	/**
	 * Calculates the x offset needed to center the text.
	 *
	 * @param text The input text.
	 * @return The x offset to add to the text coordinates to horizontally center it.
	 */
	int center(Component text);

	interface IChromosomeRowOptions<C extends IChromosome<A>, A extends IAllele> {
		Component apply(boolean active, C chromosome, A allele, InteractableTextOptions existing, Component text);
	}
}
