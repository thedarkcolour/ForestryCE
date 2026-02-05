package forestry.arboriculture.blocks;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.core.IBlockSubtype;
import forestry.api.genetics.alleles.TreeChromosomes;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Used for the default leaf, fruit, and decorative blocks.
 * Other mods can use this class to take advantage of Forestry's built-in leaf blocks.
 */
public final class ForestryLeafType implements IBlockSubtype {
	private static final ObjectOpenHashSet<ForestryLeafType> VALUES = new ObjectOpenHashSet<>(35);

	public static final ForestryLeafType OAK = new ForestryLeafType(ForestryTreeSpecies.OAK);
	public static final ForestryLeafType DARK_OAK = new ForestryLeafType(ForestryTreeSpecies.DARK_OAK);
	public static final ForestryLeafType BIRCH = new ForestryLeafType(ForestryTreeSpecies.BIRCH);
	public static final ForestryLeafType LIME = new ForestryLeafType(ForestryTreeSpecies.LIME);
	public static final ForestryLeafType WALNUT = new ForestryLeafType(ForestryTreeSpecies.WALNUT);
	public static final ForestryLeafType CHESTNUT = new ForestryLeafType(ForestryTreeSpecies.CHESTNUT);
	public static final ForestryLeafType CHERRY_VANILLA = new ForestryLeafType(ForestryTreeSpecies.CHERRY_VANILLA);
	public static final ForestryLeafType SOUR_CHERRY = new ForestryLeafType(ForestryTreeSpecies.SOUR_CHERRY);
	public static final ForestryLeafType LEMON = new ForestryLeafType(ForestryTreeSpecies.LEMON);
	public static final ForestryLeafType PLUM = new ForestryLeafType(ForestryTreeSpecies.PLUM);
	public static final ForestryLeafType MAPLE = new ForestryLeafType(ForestryTreeSpecies.MAPLE);
	public static final ForestryLeafType SPRUCE = new ForestryLeafType(ForestryTreeSpecies.SPRUCE);
	public static final ForestryLeafType LARCH = new ForestryLeafType(ForestryTreeSpecies.LARCH);
	public static final ForestryLeafType PINE = new ForestryLeafType(ForestryTreeSpecies.PINE);
	public static final ForestryLeafType SEQUOIA = new ForestryLeafType(ForestryTreeSpecies.SEQUOIA);
	public static final ForestryLeafType GIANT_SEQUOIA = new ForestryLeafType(ForestryTreeSpecies.GIANT_SEQUOIA);
	public static final ForestryLeafType JUNGLE = new ForestryLeafType(ForestryTreeSpecies.JUNGLE);
	public static final ForestryLeafType TEAK = new ForestryLeafType(ForestryTreeSpecies.TEAK);
	public static final ForestryLeafType IPE = new ForestryLeafType(ForestryTreeSpecies.IPE);
	public static final ForestryLeafType KAPOK = new ForestryLeafType(ForestryTreeSpecies.KAPOK);
	public static final ForestryLeafType EBONY = new ForestryLeafType(ForestryTreeSpecies.EBONY);
	public static final ForestryLeafType ZEBRANO = new ForestryLeafType(ForestryTreeSpecies.ZEBRANO);
	public static final ForestryLeafType MAHOGANY = new ForestryLeafType(ForestryTreeSpecies.MAHOGANY);
	public static final ForestryLeafType ACACIA_VANILLA = new ForestryLeafType(ForestryTreeSpecies.ACACIA_VANILLA);
	public static final ForestryLeafType CAMELTHORN = new ForestryLeafType(ForestryTreeSpecies.CAMELTHORN);
	public static final ForestryLeafType PADAUK = new ForestryLeafType(ForestryTreeSpecies.PADAUK);
	public static final ForestryLeafType BALSA = new ForestryLeafType(ForestryTreeSpecies.BALSA);
	public static final ForestryLeafType COCOBOLO = new ForestryLeafType(ForestryTreeSpecies.COCOBOLO);
	public static final ForestryLeafType WENGE = new ForestryLeafType(ForestryTreeSpecies.WENGE);
	public static final ForestryLeafType BAOBAB = new ForestryLeafType(ForestryTreeSpecies.BAOBAB);
	public static final ForestryLeafType MAHOE = new ForestryLeafType(ForestryTreeSpecies.MAHOE);
	public static final ForestryLeafType WILLOW = new ForestryLeafType(ForestryTreeSpecies.WILLOW);
	public static final ForestryLeafType GREENHEART = new ForestryLeafType(ForestryTreeSpecies.GREENHEART);
	public static final ForestryLeafType PAPAYA = new ForestryLeafType(ForestryTreeSpecies.PAPAYA);
	public static final ForestryLeafType DATE = new ForestryLeafType(ForestryTreeSpecies.DATE);
	public static final ForestryLeafType POPLAR = new ForestryLeafType(ForestryTreeSpecies.POPLAR);
	public static final ForestryLeafType ELM = new ForestryLeafType(ForestryTreeSpecies.ELM);
	public static final ForestryLeafType FIR = new ForestryLeafType(ForestryTreeSpecies.FIR);
	public static final ForestryLeafType COCONUT = new ForestryLeafType(ForestryTreeSpecies.COCONUT);
	public static final ForestryLeafType BEECH = new ForestryLeafType(ForestryTreeSpecies.BEECH);
	public static final ForestryLeafType FEIJOA = new ForestryLeafType(ForestryTreeSpecies.FEIJOA);
	public static final ForestryLeafType DOGWOOD = new ForestryLeafType(ForestryTreeSpecies.DOGWOOD);
	public static final ForestryLeafType GINKGO = new ForestryLeafType(ForestryTreeSpecies.GINKGO);
	public static final ForestryLeafType JACARANDA = new ForestryLeafType(ForestryTreeSpecies.JACARANDA);
	public static final ForestryLeafType PEWEN = new ForestryLeafType(ForestryTreeSpecies.PEWEN);
	public static final ForestryLeafType MACROCARPA = new ForestryLeafType(ForestryTreeSpecies.MACROCARPA);
	public static final ForestryLeafType OLIVE = new ForestryLeafType(ForestryTreeSpecies.OLIVE);
	public static final ForestryLeafType ORANGE = new ForestryLeafType(ForestryTreeSpecies.ORANGE);
	public static final ForestryLeafType PEAR = new ForestryLeafType(ForestryTreeSpecies.PEAR);
	public static final ForestryLeafType KAURI = new ForestryLeafType(ForestryTreeSpecies.KAURI);

	private final ResourceLocation speciesId;

	// These fields are initialized later in setSpecies
	private IFruit fruit;
	private ITree individual;

	// Take care not to create duplicates...
	public ForestryLeafType(ResourceLocation speciesId) {
		this.speciesId = speciesId;
		VALUES.add(this);
	}

	public void setSpecies(ITreeSpecies species) {
		this.fruit = species.getDefaultGenome().getActiveValue(TreeChromosomes.FRUIT);
		this.individual = species.createIndividual();
	}

	@Override
	public String getSerializedName() {
		return this.speciesId.getPath();
	}

	public IFruit getFruit() {
		return this.fruit;
	}

	public ITree getIndividual() {
		return this.individual;
	}

	public ResourceLocation getSpeciesId() {
		return this.speciesId;
	}

	// Used by ITreeSpeciesType to set the species of each type
	public static Set<ForestryLeafType> allValues() {
		return Collections.unmodifiableSet(VALUES);
	}

	// Default values used by Forestry to make its leaf blocks (includes all the fields)
	@ApiStatus.Internal
	public static List<ForestryLeafType> values() {
		return Arrays.asList(OAK, DARK_OAK, BIRCH, LIME, WALNUT, CHESTNUT, CHERRY_VANILLA, SOUR_CHERRY,
				LEMON, PLUM, MAPLE, SPRUCE, LARCH, PINE, SEQUOIA, GIANT_SEQUOIA, JUNGLE, TEAK, IPE, KAPOK,
				EBONY, ZEBRANO, MAHOGANY, ACACIA_VANILLA, CAMELTHORN, PADAUK, BALSA, COCOBOLO, WENGE,
				BAOBAB, MAHOE, WILLOW, GREENHEART, PAPAYA, DATE, POPLAR, ELM, FIR, COCONUT, BEECH, FEIJOA,
				DOGWOOD, GINKGO, JACARANDA, PEWEN, MACROCARPA, OLIVE, ORANGE, PEAR, KAURI);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ForestryLeafType that = (ForestryLeafType) o;

		return this.speciesId.equals(that.speciesId);
	}

	@Override
	public int hashCode() {
		return this.speciesId.hashCode();
	}
}
