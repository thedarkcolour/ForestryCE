package forestry.core.genetics.alleles;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import forestry.api.genetics.alleles.IBooleanAllele;
import forestry.api.genetics.alleles.IBooleanChromosome;

public record BooleanChromosome(ResourceLocation id, String translationKey) implements IBooleanChromosome {
	public static BooleanChromosome create(ResourceLocation id) {
		return new BooleanChromosome(id, Util.makeDescriptionId("chromosome", id));
	}

	@Override
	public String getTranslationKey(IBooleanAllele allele) {
		return allele.value() ? "allele.forestry.true" : "allele.forestry.false";
	}

	@Override
	public String getChromosomeTranslationKey() {
		return this.translationKey;
	}

	@Override
	public Class<?> valueClass() {
		return boolean.class;
	}
}
