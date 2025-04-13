package forestry.core.utils;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;

import net.minecraftforge.common.util.Lazy;

import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.alleles.IRegistryAllele;
import forestry.api.genetics.alleles.IRegistryChromosome;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.api.plugin.IGenomeBuilder;
import forestry.core.config.ForestryConfig;

public class SpeciesUtil {
	public static final Lazy<IBeeSpeciesType> BEE_TYPE = Lazy.of(() -> IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.BEE, IBeeSpeciesType.class));
	public static final Lazy<ITreeSpeciesType> TREE_TYPE = Lazy.of(() -> IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.TREE, ITreeSpeciesType.class));
	public static final Lazy<IButterflySpeciesType> BUTTERFLY_TYPE = Lazy.of(() -> IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.BUTTERFLY, IButterflySpeciesType.class));

	public static ITreeSpecies getTreeSpecies(ResourceLocation id) {
		return TREE_TYPE.get().getSpecies(id);
	}

	public static List<ITreeSpecies> getAllTreeSpecies() {
		return TREE_TYPE.get().getAllSpecies();
	}

	public static IBeeSpecies getBeeSpecies(ResourceLocation id) {
		return BEE_TYPE.get().getSpecies(id);
	}

	public static List<IBeeSpecies> getAllBeeSpecies() {
		return BEE_TYPE.get().getAllSpecies();
	}

	public static IButterflySpecies getButterflySpecies(ResourceLocation id) {
		return BUTTERFLY_TYPE.get().getSpecies(id);
	}

	public static List<IButterflySpecies> getAllButterflySpecies() {
		return BUTTERFLY_TYPE.get().getAllSpecies();
	}

	// Retrieves a species of an arbitrary type based on its allele. Does not null check.
	@Nullable
	public static ISpecies<?> getAnySpecies(ResourceLocation id) {
		@SuppressWarnings("unchecked")
		IRegistryAllele<ISpecies<?>> allele = ((IRegistryAllele<ISpecies<?>>) ForestryAlleles.REGISTRY.getAllele(id));
		return allele == null ? null : allele.value();
	}

	/**
	 * Adds all non-hidden species from the given species type to the creative tab.
	 *
	 * @param items The creative tab item output.
	 * @param speciesTypeId The ID of the species type to use.
	 */
	public static void addTypeToCreativeTab(CreativeModeTab.Output items, ResourceLocation speciesTypeId) {
		ISpeciesType<?, ?> speciesType = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(speciesTypeId);

		for (ILifeStage stage : speciesType.getLifeStages()) {
			for (ISpecies<?> species : speciesType.getAllSpecies()) {
				items.accept(species.createStack(stage));
			}
		}
	}

	@Nullable
	public static <I extends IIndividual> Tag serializeIndividual(I individual) {
		@SuppressWarnings("unchecked")
		Codec<I> individualCodec = (Codec<I>) individual.getType().getIndividualCodec();
		return individualCodec.encodeStart(NbtOps.INSTANCE, individual).result().orElse(null);
	}


	public static <I extends IIndividual> I deserializeIndividual(ISpeciesType<?, I> type, Tag nbt) {
		return type.getIndividualCodec().decode(NbtOps.INSTANCE, nbt).resultOrPartial(Forestry.LOGGER::error).orElseThrow().getFirst();
	}

	@Nullable
	public static <S extends ISpecies<?>> ImmutableList<AllelePair<?>> mutateSpecies(Level level, BlockPos pos, @Nullable GameProfile profile, IGenome parent1, IGenome parent2, IRegistryChromosome<S> speciesChromosome, IMutationChanceGetter<S> chanceGetter) {
		IGenome firstGenome;
		IGenome secondGenome;

		S firstParent;
		S secondParent;

		if (level.random.nextBoolean()) {
			firstParent = parent1.getActiveValue(speciesChromosome);
			secondParent = parent2.getInactiveValue(speciesChromosome);

			firstGenome = parent1;
			secondGenome = parent2;
		} else {
			firstParent = parent2.getActiveValue(speciesChromosome);
			secondParent = parent1.getInactiveValue(speciesChromosome);

			firstGenome = parent2;
			secondGenome = parent1;
		}

		ISpeciesType<S, ?> speciesType = parent1.getActiveSpecies().getType().cast();
		IBreedingTracker tracker = profile == null ? null : speciesType.getBreedingTracker(level, profile);
		IClimateProvider climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(level, pos);

		for (IMutation<S> mutation : speciesType.getMutations().getCombinationsShuffled(firstParent, secondParent, level.random)) {
			float chance = chanceGetter.getChance(mutation, level, pos, firstGenome, secondGenome, climate);
			if (chance <= 0) {
				continue;
			}
			if (tracker != null && tracker.isResearched(mutation)) {
				float mutationBoost = chance * (ForestryConfig.SERVER.researchMutationBoostMultiplier.get().floatValue() - 1.0f);
				mutationBoost = Math.min(ForestryConfig.SERVER.maxResearchMutationBoostPercent.get().floatValue(), mutationBoost);
				chance += mutationBoost;
			}
			if (chance > level.random.nextFloat()) {
				if (tracker != null) {
					tracker.registerMutation(mutation);
				}
				return mutation.getResultAlleles();
			}
		}

		return null;
	}

	@FunctionalInterface
	public interface IMutationChanceGetter<S extends ISpecies<?>> {
		float getChance(IMutation<S> mutation, Level level, BlockPos pos, IGenome firstGenome, IGenome secondGenome, IClimateProvider climate);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <I extends IIndividual> I createOffspring(RandomSource rand, IGenome self, IGenome mate, ISpeciesMutator mutator, Function<IGenome, I> individualFactory, boolean makeHaploid) {
		IKaryotype karyotype = self.getKaryotype();
		IGenomeBuilder genome = karyotype.createGenomeBuilder();
		ImmutableList<AllelePair<?>> parent1 = self.getAllelePairs();
		ImmutableList<AllelePair<?>> parent2 = mate.getAllelePairs();
		boolean didMutate1 = false;
		boolean didMutate2 = false;

		// Check for mutation. Replace one of the parents with the mutation
		// template if mutation occurred.
		// Haploid drones cant mutate as they only have 1 parent
		if (!makeHaploid) {
			ImmutableList<AllelePair<?>> mutated1 = mutator.mutateSpecies(self, mate);
			if (mutated1 != null) {
				parent1 = mutated1;
				didMutate1 = true;
			}
			ImmutableList<AllelePair<?>> mutated2 = mutator.mutateSpecies(mate, self);
			if (mutated2 != null) {
				parent2 = mutated2;
				didMutate2 = true;
			}
		}
		ImmutableList<IChromosome<?>> chromosomes = karyotype.getChromosomes();
		for (int i = 0; i < chromosomes.size(); i++) {
			IChromosome<?> chromosome = chromosomes.get(i);
			// unchecked due to generics being a pain
			AllelePair allele1 = parent1.get(i);
			AllelePair allele2 = parent2.get(i);
			IAllele defaultAllele = karyotype.getDefaultAllele(chromosome);

			if (!makeHaploid && karyotype.isWeaklyInherited(chromosome)) {
				// Mutation Template is homozygous so only need to check active
				if (didMutate1 && allele1.active() == defaultAllele) {
					allele1 = self.getAllelePair(chromosome);
				}
				if (didMutate2 && allele2.active() == defaultAllele) {
					allele2 = mate.getAllelePair(chromosome);
				}
			}
			genome.setUnchecked(chromosome, makeHaploid ? allele1.inheritHaploid(rand) : allele1.inheritOther(rand, allele2));
		}

		return individualFactory.apply(genome.build());
	}

	public static <I extends IIndividual> I createOffspring(RandomSource rand, IGenome self, IGenome mate, ISpeciesMutator mutator, Function<IGenome, I> individualFactory) {
		return createOffspring(rand, self, mate, mutator, individualFactory, false);
	}

	@FunctionalInterface
	public interface ISpeciesMutator {
		@Nullable
		ImmutableList<AllelePair<?>> mutateSpecies(IGenome p1, IGenome p2);
	}
}
