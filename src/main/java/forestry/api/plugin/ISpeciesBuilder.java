package forestry.api.plugin;

import com.google.common.collect.ImmutableMap;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;

/**
 * Customize properties shared by all species types. Implement this class for your species type registration.
 */
public interface ISpeciesBuilder<T extends ISpeciesType<S, ?>, S extends ISpecies<?>, B extends ISpeciesBuilder<T, S, B>> {
	/**
	 * Overrides whether the allele of this species is dominant. Usually set first by a "registerSpecies" method parameter.
	 */
	B setDominant(boolean dominant);

	/**
	 * Customize the default alleles for each chromosome in the karyotype.
	 * Called after {@link ITaxonBuilder#setDefaultChromosome} which in turn is called after {@link ISpeciesTypeBuilder#setKaryotype}.
	 */
	B setGenome(Consumer<IGenomeBuilder> genome);

	/**
	 * Define mutations that mutate into this species.
	 */
	B addMutations(Consumer<IMutationsRegistration> mutations);

	/**
	 * Specify whether item forms of this species will have an enchantment glint.
	 * Usually for rare/milestone species like Steadfast or Industrious.
	 * For trees and butterflies, only applies to their item forms, not their block/entity forms.
	 */
	B setGlint(boolean glint);

	/**
	 * Sets the ideal temperature preference of this species.
	 * All species types in base Forestry use climate information.
	 * If a species type from an addon doesn't use this information, this method does nothing.
	 */
	B setTemperature(TemperatureType temperature);

	/**
	 * Sets the ideal humidity preference of this species.
	 * All species types in base Forestry use climate information.
	 * If a species type from an addon doesn't use this information, this method does nothing.
	 */
	B setHumidity(HumidityType humidity);

	/**
	 * Overrides the default complexity of this species, a number between 1 and 10 used by the Escritoire
	 * to determine how many cells the memory game has when analyzing a specimen of this species.
	 * By default, complexity is determined by the number of breeding steps required to breed this species.
	 */
	B setComplexity(int complexity);

	/**
	 * Sets the escritoire color of this species. Used for Escritoire game cells.
	 * If this method is never called, then the default escritoire color is {@code -1}.
	 * Usually, this method is optional and the color of the game cell is fetched from some other property.
	 * For example, bees use their outline color, and butterflies use their serum color. However, this property
	 * must be set manually in the case of tree species.
	 *
	 * @param color The color of the Escritoire memory cell when displaying this species.
	 */
	B setEscritoireColor(TextColor color);

	/**
	 * @deprecated Use the variant that accepts a TextColor
	 */
	@Deprecated(forRemoval = true)
	default B setEscritoireColor(Color color) {
		return setEscritoireColor(TextColor.fromRgb(color.getRGB()));
	}

	/**
	 * Specify whether this bee species is a "secret" species whose mutation cannot be discovered in the Escritoire.
	 * <p>
	 * Example species include: <ul>
	 * <li>Holiday species (Leporine, Merry, Tricky, Tipsy)</li>
	 * <li>Iridium-producing species (Vindictive, Vengeful, Avenging)</li>
	 * <li>Easter eggs like the secret Benson species from Career Bees</li>
	 * </ul>
	 */
	B setSecret(boolean secret);

	/**
	 * Sets the name of this species's discoverer. Most of the time, it is either Sengir, Binnie, or MysteriousAges.
	 *
	 * @param authority The name of the person who discovered this bee species.
	 */
	B setAuthority(String authority);

	/**
	 * Use a custom class for this species. Default is usually something like {@code BeeSpecies::new}.
	 */
	B setFactory(ISpeciesFactory<T, S, B> factory);

	/**
	 * @return The scientific name of the genus this species belongs to. Usually set when the builder is created.
	 */
	String getGenus();

	/**
	 * @return The scientific name of the species without the genus. Usually set when the builder is created.
	 */
	String getSpecies();

	/**
	 * @return Whether alleles of this species are dominant. Set in {@link #setDominant}.
	 */
	boolean isDominant();

	/**
	 * Used to create the default genome object for this species during registration.
	 *
	 * @param builder A genome builder with alleles set first by the karyotype, then by the parent taxa of this species.
	 * @return A completed default genome for this species, with all alleles that have been set in {@link #setGenome}.
	 */
	IGenome buildGenome(IGenomeBuilder builder);

	/**
	 * @return Whether the item form of this species has an enchantment glint. Set in {@link #setGlint}.
	 */
	boolean hasGlint();

	TemperatureType getTemperature();

	HumidityType getHumidity();

	int getComplexity();

	/**
	 * @return The escritoire cell color of this species, or {@code -1} if {@link #setEscritoireColor} was not called.
	 */
	int getEscritoireColor();

	/**
	 * @return Whether this species should be hidden in creative menus or JEI. Set in {@link #setSecret}.
	 */
	boolean isSecret();

	/**
	 * @return The name of who created this species. The default is "SirSengir". Set in {@link #setAuthority}.
	 */
	String getAuthority();

	ISpeciesFactory<T, S, B> createSpeciesFactory();

	List<IMutation<S>> buildMutations(ISpeciesType<S, ?> speciesType, ImmutableMap<ResourceLocation, S> speciesLookup);

	@FunctionalInterface
	interface ISpeciesFactory<T extends ISpeciesType<S, ?>, S extends ISpecies<?>, B extends ISpeciesBuilder<T, S, B>> {
		S create(ResourceLocation id, T speciesType, IGenome defaultGenome, B builder);
	}
}
