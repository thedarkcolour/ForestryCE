package forestry.arboriculture.blocks;

import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.core.IBlockSubtype;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.IValueAllele;

import java.util.Locale;

public enum ForestryPodType implements IBlockSubtype {
	COCOA(ForestryAlleles.FRUIT_COCOA),
	//TODO: change all of these to be 'bunches'. Could also be used for Bananas?
	DATES(ForestryAlleles.FRUIT_DATES),
	PAPAYA(ForestryAlleles.FRUIT_PAPAYA),
	COCONUT(ForestryAlleles.FRUIT_COCONUT);

	private final IValueAllele<IFruit> allele;

	ForestryPodType(IValueAllele<IFruit> allele) {
		this.allele = allele;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public IFruit getFruit() {
		return this.allele.value();
	}
}
