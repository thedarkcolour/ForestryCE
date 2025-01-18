package forestry.apiimpl.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
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
import forestry.api.plugin.IGenomeBuilder;
import forestry.api.plugin.IMutationsRegistration;
import forestry.api.plugin.ISpeciesBuilder;

public abstract class SpeciesBuilder<T extends ISpeciesType<S, ?>, S extends ISpecies<?>, B extends ISpeciesBuilder<T, S, B>> implements ISpeciesBuilder<T, S, B> {
	protected final ResourceLocation id;
	protected final String genus;
	protected final String species;
	protected final MutationsRegistration mutations;

	protected TemperatureType temperature = TemperatureType.NORMAL;
	protected HumidityType humidity = HumidityType.NORMAL;
	protected boolean dominant;
	protected boolean glint;
	protected int complexity;
	protected boolean secret;
	protected String authority = "Sengir";
	@Nullable
	protected Consumer<IGenomeBuilder> genome = null;
	protected ISpeciesFactory<T, S, B> factory;
	protected int escritoireColor = -1;

	public SpeciesBuilder(ResourceLocation id, String genus, String species, MutationsRegistration mutations) {
		this.id = id;
		this.genus = genus;
		this.species = species;
		this.mutations = mutations;
		this.factory = createSpeciesFactory();
	}

	@Override
	public B setDominant(boolean dominant) {
		this.dominant = dominant;
		return self();
	}

	@Override
	public B setGenome(Consumer<IGenomeBuilder> genome) {
		this.genome = this.genome == null ? genome : this.genome.andThen(genome);
		return self();
	}

	@Override
	public B addMutations(Consumer<IMutationsRegistration> mutations) {
		mutations.accept(this.mutations);
		return self();
	}

	@Override
	public B setGlint(boolean glint) {
		this.glint = glint;
		return self();
	}

	@Override
	public B setTemperature(TemperatureType temperature) {
		this.temperature = temperature;
		return self();
	}

	@Override
	public B setHumidity(HumidityType humidity) {
		this.humidity = humidity;
		return self();
	}

	@Override
	public B setComplexity(int complexity) {
		this.complexity = complexity;
		return self();
	}

	@Override
	public B setEscritoireColor(TextColor color) {
		this.escritoireColor = color.getValue();
		return self();
	}

	@Override
	public B setSecret(boolean secret) {
		this.secret = secret;
		return self();
	}

	@Override
	public B setAuthority(String authority) {
		this.authority = authority;
		return self();
	}

	@Override
	public B setFactory(ISpeciesFactory<T, S, B> factory) {
		this.factory = Preconditions.checkNotNull(factory);
		return self();
	}

	@SuppressWarnings("unchecked")
	protected B self() {
		return (B) this;
	}

	@Override
	public TemperatureType getTemperature() {
		return this.temperature;
	}

	@Override
	public HumidityType getHumidity() {
		return this.humidity;
	}

	@Override
	public int getComplexity() {
		return this.complexity;
	}

	@Override
	public int getEscritoireColor() {
		return this.escritoireColor;
	}

	@Override
	public String getGenus() {
		return this.genus;
	}

	@Override
	public String getSpecies() {
		return this.species;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	@Override
	public IGenome buildGenome(IGenomeBuilder builder) {
		if (this.genome != null) {
			this.genome.accept(builder);
		}
		try {
			return builder.build();
		} catch (Throwable t) {
			throw new RuntimeException("Error trying to register species with ID " + this.id, t);
		}
	}

	@Override
	public boolean hasGlint() {
		return this.glint;
	}

	@Override
	public boolean isSecret() {
		return this.secret;
	}

	@Override
	public String getAuthority() {
		return this.authority;
	}

	@Override
	public List<IMutation<S>> buildMutations(ISpeciesType<S, ?> speciesType, ImmutableMap<ResourceLocation, S> speciesLookup) {
		return this.mutations.build(speciesType, speciesLookup);
	}
}
