package forestry.api.genetics.alleles;

import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.genetics.ForestrySpeciesTypes;

import static forestry.api.ForestryConstants.forestry;

public class TreeChromosomes {
	/**
	 * The species of a tree.
	 */
	public static final IRegistryChromosome<ITreeSpecies> SPECIES = ForestryAlleles.REGISTRY.registryChromosome(ForestrySpeciesTypes.TREE, ITreeSpecies.class);
	/**
	 * Modifies the height of a tree.
	 */
	public static final IFloatChromosome HEIGHT = ForestryAlleles.REGISTRY.floatChromosome(forestry("height"));
	/**
	 * Chance for saplings.
	 */
	public static final IFloatChromosome SAPLINGS = ForestryAlleles.REGISTRY.floatChromosome(forestry("saplings"));
	/**
	 * Determines what fruits are grown on the tree.
	 */
	public static final IRegistryChromosome<IFruit> FRUIT = ForestryAlleles.REGISTRY.registryChromosome(forestry("fruits"), IFruit.class);
	/**
	 * Chance for fruit leaves and/or drops.
	 */
	public static final IFloatChromosome YIELD = ForestryAlleles.REGISTRY.floatChromosome(forestry("yield"));
	/**
	 * Determines the speed at which fruit will ripen on this tree.
	 */
	public static final IFloatChromosome SAPPINESS = ForestryAlleles.REGISTRY.floatChromosome(forestry("sappiness"));
	/**
	 * Unimplemented. All trees added by base Forestry have {@link ForestryAlleles#TREE_EFFECT_NONE}.
	 */
	public static final IRegistryChromosome<ITreeEffect> EFFECT = ForestryAlleles.REGISTRY.registryChromosome(forestry("tree_effect"), ITreeEffect.class);
	/**
	 * Amount of random ticks which need to elapse before a sapling will grow into a tree.
	 */
	public static final IIntegerChromosome MATURATION = ForestryAlleles.REGISTRY.intChromosome(forestry("maturation"));
	/**
	 * The diameter of the tree. If the allele is 2, then the tree trunk is a 2x2 and requires four saplings to grow.
	 */
	public static final IIntegerChromosome GIRTH = ForestryAlleles.REGISTRY.intChromosome(forestry("girth"));
	/**
	 * Determines if the tree can burn.
	 */
	public static final IBooleanChromosome FIREPROOF = ButterflyChromosomes.FIREPROOF;
}
