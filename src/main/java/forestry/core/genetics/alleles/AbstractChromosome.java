package forestry.core.genetics.alleles;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.core.utils.GeneticsUtil;

public abstract class AbstractChromosome<A extends IAllele> implements IChromosome<A> {
	protected final ResourceLocation id;
	private final String translationKey;

	protected AbstractChromosome(ResourceLocation id) {
		this.id = id;
		this.translationKey = Util.makeDescriptionId("chromosome", this.id);
	}

	@Override
	public ResourceLocation id() {
		return this.id;
	}

	@Override
	public String getTranslationKey(A allele) {
		return GeneticsUtil.createTranslationKey("allele", this.id, allele.alleleId());
	}

	@Override
	public String getChromosomeTranslationKey() {
		return this.translationKey;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.id + ']';
	}
}
