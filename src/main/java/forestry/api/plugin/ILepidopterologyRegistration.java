package forestry.api.plugin;

import java.awt.Color;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;

public interface ILepidopterologyRegistration {
	/**
	 * @deprecated Use the variant that accepts a TextColor
	 */
	@Deprecated(forRemoval = true)
	default IButterflySpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, Color serumColor, float rarity) {
		return registerSpecies(id, genus, species, dominant, TextColor.fromRgb(serumColor.getRGB()), rarity);
	}

	/**
	 * Register a new butterfly species.
	 *
	 * @param id         The unique ID for this species.
	 * @param genus      The scientific name of the genus containing this species. See {@link forestry.api.genetics.ForestryTaxa}.
	 * @param species    The scientific name of the species without the genus.
	 * @param dominant   Whether this species appears as a dominant allele in the genome.
	 * @param serumColor The color of this butterfly's serum.
	 * @param rarity     The rarity of this species for spawning.
	 */
	IButterflySpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor serumColor, float rarity);

	void registerCocoon(ResourceLocation id, IButterflyCocoon cocoon);

	void registerEffect(ResourceLocation id, IButterflyEffect effect);
}
