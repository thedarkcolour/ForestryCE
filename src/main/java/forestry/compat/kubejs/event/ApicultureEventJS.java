package forestry.compat.kubejs.event;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Function3;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.LightPreference;
import forestry.api.apiculture.hives.IHiveGen;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.api.plugin.IHiveBuilder;
import forestry.compat.kubejs.apiculture.KubeActivityType;
import forestry.compat.kubejs.apiculture.KubeBeeEffect;
import forestry.compat.kubejs.apiculture.KubeFlowerType;
import forestry.compat.kubejs.apiculture.KubeHiveDefinition;

import dev.latvian.mods.kubejs.event.EventJS;

public class ApicultureEventJS extends EventJS {
	private final IApicultureRegistration wrapped;

	public ApicultureEventJS(IApicultureRegistration wrapped) {
		this.wrapped = wrapped;
	}

	/**
	 * Used to register a new species. Returns a bee species builder that can be further customized with the methods
	 * found in {@link IBeeSpeciesBuilder} and {@link forestry.api.plugin.ISpeciesBuilder}.
	 *
	 * @param id       The unique ID of the species.
	 * @param genus    The lowercase name of this species's taxonomic genus. See {@link forestry.api.genetics.ForestryTaxa}
	 *                 for examples. The genus is used to group similar bees together (ex. the HEROIC bees) and can
	 *                 specify default alleles for all bees sharing that genus. <br>
	 *                 To use a custom genus, you must define its taxon in {@link GeneticsEventJS}.
	 * @param species  The lowercase name of the taxonomic species. See {@link forestry.api.genetics.ForestryTaxa} for
	 *                 examples. This is really only used in the fifth tab of the analyzer. Not really important, but
	 *                 it must be different from any other bee species.
	 * @param dominant Whether the allele for this species is dominant or recessive. Used during inheritance.
	 * @param outline  The color of the bee's outline. Stripe and body colors can be customized using the returned {@link IBeeSpeciesBuilder}.
	 * @return A bee species builder which you can further customize with mutations, products, and other properties.
	 */
	public IBeeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor outline) {
		return this.wrapped.registerSpecies(id, genus, species, dominant, outline);
	}

	public void modifySpecies(ResourceLocation id, Consumer<IBeeSpeciesBuilder> action) {
		this.wrapped.modifySpecies(id, action);
	}

	public void addVillageBee(ResourceLocation speciesId, boolean rare, Map<IChromosome<?>, IAllele> alleles) {
		this.wrapped.addVillageBee(speciesId, rare, alleles);
	}

	public IHiveBuilder registerCustomHive(ResourceLocation id, IHiveGen placement, BlockState hiveState, Predicate<Holder<Biome>> isGoodBiome, Predicate<HumidityType> isGoodHumidity, Predicate<TemperatureType> isGoodTemperature, float genChance, KubeHiveDefinition.PostGenFunction postGen) {
		return this.wrapped.registerHive(id, new KubeHiveDefinition(placement, hiveState, isGoodBiome, isGoodHumidity, isGoodTemperature, genChance, postGen));
	}

	public void modifyHive(ResourceLocation id, Consumer<IHiveBuilder> action) {
		this.wrapped.modifyHive(id, action);
	}

	public void registerFlowerType(ResourceLocation id, BiPredicate<Level, BlockPos> isAcceptableFlower, KubeFlowerType.PlantRandomFlowerFunction plantRandomFlower, boolean dominant) {
		this.wrapped.registerFlowerType(id, new KubeFlowerType(isAcceptableFlower, plantRandomFlower, dominant));
	}

	public void registerBeeEffect(ResourceLocation id, UnaryOperator<IEffectData> validateStorage, boolean combinable,
			Function3<IGenome, IEffectData, IBeeHousing, IEffectData> doEffect,
			Function3<IGenome, IEffectData, IBeeHousing, IEffectData> doClientEffect,
			boolean dominant) {
		this.wrapped.registerBeeEffect(id, new KubeBeeEffect(validateStorage, combinable, doEffect, doClientEffect, dominant));
	}

	public void registerCustomActivityType(ResourceLocation id, KubeActivityType.IsActiveFunction isActive, KubeActivityType.InactiveErrorFunction inactiveErrorFunction, LightPreference lightPreference, boolean dominant) {
		this.wrapped.registerActivityType(id, new KubeActivityType(isActive, inactiveErrorFunction, lightPreference, dominant));
	}

	public void registerSwarmerMaterial(Item swarmItem, float swarmChance) {
		this.wrapped.registerSwarmerMaterial(swarmItem, swarmChance);
	}
}
