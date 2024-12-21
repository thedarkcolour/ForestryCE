package forestry.core.genetics;

import java.util.List;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import forestry.api.IForestryApi;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.ITaxon;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IRegistryChromosome;
import forestry.api.genetics.alleles.IValueAllele;
import forestry.api.plugin.ISpeciesBuilder;
import forestry.core.utils.GeneticsUtil;

public abstract class Species<T extends ISpeciesType<? extends ISpecies<I>, I>, I extends IIndividual> implements ISpecies<I> {
	protected final ResourceLocation id;
	protected final T speciesType;
	protected final IGenome defaultGenome;
	private int complexity;
	protected final int escritoireColor;
	protected final boolean secret;
	protected final boolean glint;
	protected final boolean dominant;
	protected final String authority;
	protected final String species;
	protected final ITaxon genus;
	protected final String binomial;
	protected final String translationKey;

	public Species(ResourceLocation id, T speciesType, IGenome defaultGenome, ISpeciesBuilder<T, ?, ?> builder) {
		this.id = id;
		this.speciesType = speciesType;
		this.defaultGenome = defaultGenome;

		this.complexity = builder.getComplexity();
		this.escritoireColor = builder.getEscritoireColor();
		this.secret = builder.isSecret();
		this.glint = builder.hasGlint();
		this.dominant = builder.isDominant();
		this.authority = builder.getAuthority();
		this.species = builder.getSpecies();
		this.genus = IForestryApi.INSTANCE.getGeneticManager().getTaxon(builder.getGenus());
		this.binomial = createBinomial(this.genus.name(), this.species);
		this.translationKey = GeneticsUtil.createTranslationKey("allele", speciesType.id(), id);
	}

	private static String createBinomial(String genus, String species) {
		@SuppressWarnings("StringBufferReplaceableByString")
		StringBuilder binomial = new StringBuilder();
		binomial.append(Character.toUpperCase(genus.charAt(0)));
		// direct array copy, faster than creating a substring
		binomial.append(genus, 1, genus.length());
		binomial.append(' ');
		binomial.append(species);
		return binomial.toString();
	}

	@Override
	public String getBinomial() {
		return this.binomial;
	}

	@Override
	public String getSpeciesName() {
		return this.species;
	}

	@Override
	public ITaxon getGenus() {
		return this.genus;
	}

	@Override
	public String getTranslationKey() {
		return this.translationKey;
	}

	@Override
	public IGenome getDefaultGenome() {
		return this.defaultGenome;
	}

	@Override
	public ResourceLocation id() {
		return this.id;
	}

	@Override
	public T getType() {
		return this.speciesType;
	}

	@Override
	public boolean isSecret() {
		return this.secret;
	}

	@Override
	public int getComplexity() {
		if (this.complexity == 0) {
			this.complexity = GeneticsUtil.getResearchComplexity(this);
		}
		return this.complexity;
	}

	@Override
	public int getEscritoireColor() {
		return this.escritoireColor;
	}

	@Override
	public boolean hasGlint() {
		return this.glint;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	@Override
	public String getAuthority() {
		return this.authority;
	}

	@Override
	public I createIndividual(Map<IChromosome<?>, IAllele> alleles) {
		return createIndividual(this.defaultGenome.copyWith(alleles));
	}

	@Override
	public I createIndividualFromPairs(Map<IChromosome<?>, AllelePair<?>> allelePairs) {
		return createIndividual(this.defaultGenome.copyWithPairs(allelePairs));
	}

	protected static void addUnknownGenomeTooltip(List<Component> tooltip) {
		tooltip.add(Component.literal("<").append(Component.translatable("for.gui.unknown")).append(">").withStyle(ChatFormatting.GRAY));
	}

	protected <S extends ISpecies<?>> void addHybridTooltip(List<Component> tooltip, IGenome genome, IRegistryChromosome<S> species, String hybridKey) {
		AllelePair<IValueAllele<S>> speciesPair = genome.getAllelePair(species);
		S primary = speciesPair.active().value();
		S secondary = speciesPair.inactive().value();
		if (!speciesPair.isSameAlleles()) {
			tooltip.add(Component.translatable(hybridKey, primary.getDisplayName(), secondary.getDisplayName()).withStyle(ChatFormatting.BLUE));
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.id + ']';
	}
}
