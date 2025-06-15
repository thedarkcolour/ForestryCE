package forestry.arboriculture.blocks;

import forestry.api.arboriculture.genetics.IPodFruit;
import forestry.api.core.IBlockSubtype;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.IValueAllele;

import java.util.Locale;

public enum ForestryPodType implements IBlockSubtype {
	DATES(ForestryAlleles.FRUIT_DATES),
	PAPAYA(ForestryAlleles.FRUIT_PAPAYA);

	private final IValueAllele<IPodFruit> allele;

	ForestryPodType(IValueAllele<IPodFruit> allele) {
		this.allele = allele;
	}

	@Override
	public String identifier() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	public IPodFruit getFruit() {
		return this.allele.value();
	}
}
