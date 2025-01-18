package forestry.apiculture.compat;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IGenome;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.plugin.IGenomeBuilder;

public class MutationRecipe {
	final ItemStack firstParent;
	final ItemStack secondParent;
	final ItemStack result;
	final IMutation<?> mutation;

	public MutationRecipe(IMutation<?> mutation) {
		ISpeciesType<?, ?> speciesType = mutation.getType();

		ILifeStage firstStage = speciesType.getTypeForMutation(0);
		ILifeStage secondStage = speciesType.getTypeForMutation(1);
		ILifeStage matedStage = speciesType.getTypeForMutation(2);

		this.firstParent = MutationsRecipeCategory.createAnalyzedStack(firstStage, mutation.getFirstParent(), null);
		this.secondParent = MutationsRecipeCategory.createAnalyzedStack(secondStage, mutation.getSecondParent(), null);

		ISpecies<?> resultSpecies = mutation.getResult();
		IGenome resultGenome;

		if (mutation.getResultAlleles() == resultSpecies.getDefaultGenome().getAllelePairs()) {
			// Mutations that don't have custom alleles will use the allele pairs list from the default genome
			// see Mutation#buildResultAlleles
			resultGenome = resultSpecies.getDefaultGenome();
		} else {
			IKaryotype karyotype = speciesType.getKaryotype();
			IGenomeBuilder builder = karyotype.createGenomeBuilder();
			List<AllelePair<?>> allelePairs = mutation.getResultAlleles();

			int i = 0;
			for (IChromosome<?> chromosome : karyotype.getChromosomes()) {
				builder.setUnchecked(chromosome, allelePairs.get(i));
			}

			resultGenome = builder.build();
		}

		this.result = MutationsRecipeCategory.createAnalyzedStack(matedStage, mutation.getResult(), resultGenome);
		this.mutation = mutation;
	}
}
