package forestry.core.genetics.mutations;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.*;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.core.genetics.ItemResearchNote;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class Mutation<S extends ISpecies<?>> implements IMutation<S> {
	private final ISpeciesType<S, ?> type;
	private final float chance;
	private final List<IMutationCondition> conditions;
	private final List<Component> specialConditions;
	private final S firstParent;
	private final S secondParent;
	private final S result;
	private final ImmutableList<AllelePair<?>> resultAlleles;
	private final boolean secret;

	public Mutation(ISpeciesType<S, ?> type, S firstParent, S secondParent, S result, Map<IChromosome<?>, IAllele> resultAlleles, float chance, List<IMutationCondition> conditions) {
		this.type = type;
		this.chance = chance;
		this.conditions = conditions;
		ImmutableList.Builder<Component> specialConditions = ImmutableList.builderWithExpectedSize(conditions.size());
		for (IMutationCondition condition : conditions) {
			specialConditions.add(condition.getDescription());
		}
		this.specialConditions = specialConditions.build();
		this.firstParent = firstParent;
		this.secondParent = secondParent;
		this.result = result;
		this.resultAlleles = buildResultAlleles(type.getKaryotype(), result.getDefaultGenome(), resultAlleles);
		this.secret = result.isSecret() || firstParent.isSecret() || secondParent.isSecret();
	}

	private static ImmutableList<AllelePair<?>> buildResultAlleles(IKaryotype karyotype, IGenome defaultGenome, Map<IChromosome<?>, IAllele> resultAlleles) {
		if (resultAlleles.isEmpty()) {
			return defaultGenome.getAllelePairs();
		}
		ImmutableList.Builder<AllelePair<?>> newAlleles = ImmutableList.builderWithExpectedSize(karyotype.size());

		for (IChromosome<?> chromosome : karyotype.getChromosomes()) {
			IAllele customAllele = resultAlleles.get(chromosome);
			if (customAllele != null) {
				newAlleles.add(AllelePair.both(customAllele));
			} else {
				newAlleles.add(defaultGenome.getAllelePair(chromosome));
			}
		}

		return newAlleles.build();
	}

	public static float getChance(IMutation<?> mutation, Level level, BlockPos pos, IGenome firstGenome, IGenome secondGenome, IClimateProvider climate) {
		float mutationChance = mutation.getChance();
		for (IMutationCondition condition : mutation.getConditions()) {
			mutationChance = condition.modifyChance(level, pos, mutation, firstGenome, secondGenome, climate, mutationChance);
			if (mutationChance == 0f) {
				return 0f;
			}
		}
		return Math.max(0f, mutationChance);
	}

	@Override
	public ISpeciesType<S, ?> getType() {
		return this.type;
	}

	@Override
	public List<IMutationCondition> getConditions() {
		return this.conditions;
	}

	@Override
	public List<Component> getSpecialConditions() {
		return this.specialConditions;
	}

	@Override
	public S getFirstParent() {
		return this.firstParent;
	}

	@Override
	public S getSecondParent() {
		return this.secondParent;
	}

	@Override
	public S getResult() {
		return this.result;
	}

	@Override
	public ImmutableList<AllelePair<?>> getResultAlleles() {
		return this.resultAlleles;
	}

	@Override
	public float getChance() {
		return this.chance;
	}

	@Override
	public boolean isPartner(ISpecies<?> species) {
		return this.firstParent == species || this.secondParent == species;
	}

	@Override
	public ISpecies<?> getPartner(ISpecies<?> species) {
		if (this.firstParent == species) {
			return this.secondParent;
		} else if (this.secondParent == species) {
			return this.firstParent;
		} else {
			throw new IllegalArgumentException("Tried to get partner for allele that is not part of this mutation.");
		}
	}

	@Override
	public boolean isSecret() {
		return this.secret;
	}

	@Override
	public ItemStack getMutationNote(GameProfile researcher) {
		return ItemResearchNote.createMutationNoteStack(researcher, this);
	}
}
