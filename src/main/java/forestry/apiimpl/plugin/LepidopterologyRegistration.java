package forestry.apiimpl.plugin;

import com.google.common.collect.ImmutableMap;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import forestry.api.genetics.ISpeciesType;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.plugin.IButterflySpeciesBuilder;
import forestry.api.plugin.ILepidopterologyRegistration;

public class LepidopterologyRegistration extends SpeciesRegistration<IButterflySpeciesBuilder, IButterflySpecies, ButterflySpeciesBuilder> implements ILepidopterologyRegistration {
	private final Registrar<ResourceLocation, IButterflyCocoon, IButterflyCocoon> cocoons = new Registrar<>(IButterflyCocoon.class);
	private final Registrar<ResourceLocation, IButterflyEffect, IButterflyEffect> effects = new Registrar<>(IButterflyEffect.class);

	public LepidopterologyRegistration(ISpeciesType<IButterflySpecies, ?> type) {
		super(type);
	}

	@Override
	public IButterflySpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor serumColor, float rarity) {
		return register(id, genus, species)
				.setDominant(dominant)
				.setSerumColor(serumColor)
				.setRarity(rarity);
	}

	@Override
	public void registerCocoon(ResourceLocation id, IButterflyCocoon cocoon) {
		this.cocoons.create(id, cocoon);
	}

	@Override
	public void registerEffect(ResourceLocation id, IButterflyEffect effect) {
		this.effects.create(id, effect);
	}

	@Override
	protected ButterflySpeciesBuilder createSpeciesBuilder(ResourceLocation id, String genus, String species, MutationsRegistration mutations) {
		return new ButterflySpeciesBuilder(id, genus, species, mutations);
	}

	public ImmutableMap<ResourceLocation, IButterflyCocoon> getCocoons() {
		return this.cocoons.build();
	}

	public ImmutableMap<ResourceLocation, IButterflyEffect> getEffects() {
		return this.effects.build();
	}
}
