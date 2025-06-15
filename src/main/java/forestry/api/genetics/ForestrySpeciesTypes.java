package forestry.api.genetics;

import forestry.api.ForestryConstants;
import net.minecraft.resources.ResourceLocation;

/**
 * The three types of species registered by base Forestry.
 */
public class ForestrySpeciesTypes {
	/**
	 * @see forestry.api.apiculture.bee.IBeeSpeciesType
	 */
	public static final ResourceLocation BEE = ForestryConstants.forestry("bee_species");
	/**
	 * @see forestry.api.arboriculture.genetics.ITreeSpeciesType
	 */
	public static final ResourceLocation TREE = ForestryConstants.forestry("tree_species");
	/**
	 * @see forestry.api.lepidopterology.genetics.IButterflySpeciesType
	 */
	public static final ResourceLocation BUTTERFLY = ForestryConstants.forestry("butterfly_species");
}
