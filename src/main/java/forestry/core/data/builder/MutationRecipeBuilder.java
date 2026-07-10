package forestry.core.data.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IMutationCondition;
import forestry.api.genetics.alleles.Allele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.core.genetics.mutations.MutationConditionBiome;
import forestry.core.genetics.mutations.MutationConditionCave;
import forestry.core.genetics.mutations.MutationConditionDaytime;
import forestry.core.genetics.mutations.MutationConditionHumidity;
import forestry.core.genetics.mutations.MutationConditionRequiresResource;
import forestry.core.genetics.mutations.MutationConditionTemperature;
import forestry.core.genetics.mutations.MutationConditionTimeLimited;
import forestry.core.genetics.mutations.MutationRecipe;

/**
 * Datagen builder for a single {@link MutationRecipe}. Mirrors the old {@code IMutationBuilder} method surface so the
 * built-in mutations can be ported one-to-one.
 */
public class MutationRecipeBuilder {
	private final ResourceLocation speciesTypeId;
	private final ResourceLocation firstParentId;
	private final ResourceLocation secondParentId;
	private final ResourceLocation resultId;
	private final float chance;
	private final List<IMutationCondition> conditions = new ArrayList<>();
	private final Map<ResourceLocation, Allele<?>> resultAlleles = new LinkedHashMap<>();

	public MutationRecipeBuilder(ResourceLocation speciesTypeId, ResourceLocation firstParentId, ResourceLocation secondParentId, ResourceLocation resultId, float chance) {
		this.speciesTypeId = speciesTypeId;
		this.firstParentId = firstParentId;
		this.secondParentId = secondParentId;
		this.resultId = resultId;
		this.chance = chance;
	}

	public ResourceLocation getSpeciesTypeId() {
		return this.speciesTypeId;
	}

	public ResourceLocation getResultId() {
		return this.resultId;
	}

	public MutationRecipeBuilder temperature(TemperatureType temperature) {
		return temperature(temperature, temperature);
	}

	public MutationRecipeBuilder temperature(TemperatureType min, TemperatureType max) {
		this.conditions.add(new MutationConditionTemperature(min, max));
		return this;
	}

	public MutationRecipeBuilder humidity(HumidityType humidity) {
		return humidity(humidity, humidity);
	}

	public MutationRecipeBuilder humidity(HumidityType min, HumidityType max) {
		this.conditions.add(new MutationConditionHumidity(min, max));
		return this;
	}

	public MutationRecipeBuilder biome(TagKey<Biome> biome) {
		this.conditions.add(new MutationConditionBiome(biome));
		return this;
	}

	public MutationRecipeBuilder dateRange(int startMonth, int startDay, int endMonth, int endDay) {
		this.conditions.add(new MutationConditionTimeLimited(startMonth, startDay, endMonth, endDay));
		return this;
	}

	public MutationRecipeBuilder day() {
		this.conditions.add(new MutationConditionDaytime(true));
		return this;
	}

	public MutationRecipeBuilder night() {
		this.conditions.add(new MutationConditionDaytime(false));
		return this;
	}

	public MutationRecipeBuilder requiresResource(BlockState... acceptedBlockStates) {
		this.conditions.add(new MutationConditionRequiresResource(acceptedBlockStates));
		return this;
	}

	public MutationRecipeBuilder cave() {
		this.conditions.add(new MutationConditionCave());
		return this;
	}

	public MutationRecipeBuilder condition(IMutationCondition condition) {
		this.conditions.add(condition);
		return this;
	}

	public <V> MutationRecipeBuilder resultAllele(IChromosome<V> chromosome, Allele<V> allele) {
		this.resultAlleles.put(chromosome.id(), allele);
		return this;
	}

	public MutationRecipe build(ResourceLocation id) {
		return new MutationRecipe(this.speciesTypeId, id, this.firstParentId, this.secondParentId, this.resultId, this.chance, List.copyOf(this.conditions), Map.copyOf(this.resultAlleles));
	}
}
