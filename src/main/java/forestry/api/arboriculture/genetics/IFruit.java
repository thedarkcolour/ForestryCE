package forestry.api.arboriculture.genetics;

import forestry.api.core.IProduct;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.IRegistryAlleleValue;
import forestry.api.genetics.alleles.TreeChromosomes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Provides all information that is needed to spawn a fruit leaves in the world.
 * If your fruit is a pod fruit, like Cocoa, Dates, or Papaya, use the more sensitive {@link IPodFruit}.
 */
public interface IFruit extends IRegistryAlleleValue, IProductProducer, ISpecialtyProducer {
	/**
	 * Returns the color of the fruit spite based on the ripening time of the fruit.
	 *
	 * @param genome       The genome of the tree of the pod / leaves block.
	 * @param ripeningTime The ripening time of the leaves / pod block. From 0 to {@link #getRipeningPeriod()}.
	 */
	int getColour(IGenome genome, BlockGetter level, BlockPos pos, int ripeningTime);

	/**
	 * return the color to use for decorative leaves. Usually the ripe color.
	 */
	int getDecorativeColor();

	/**
	 * Determines if fruit block of this provider is considered a leaf block.
	 *
	 * @return True if this provider provides a fruit leaf for the given genome at the given position.
	 */
	boolean isFruitLeaf();

	/**
	 * The chance that a tree leaf will spawn with fruits or the chance that a pod fruit spawns.
	 *
	 * @param genome The genome of the tree of the pod / leaves block.
	 * @param level  The world where this is happening.
	 * @return The chance that leaves spawns with fruits or the chance that a pod block spawns.
	 */
	float getFruitChance(IGenome genome, LevelAccessor level);

	/**
	 * @return How many successful ripening block ticks a fruit needs to be ripe.
	 */
	int getRipeningPeriod();

	/**
	 * A unmodifiable list that contains all specialties and their associated drop chances.
	 *
	 * @return A unmodifiable list that contains all products and their associated drop chances.
	 * @deprecated Use {@link #getSpecialties} instead.
	 */
	@Deprecated
	List<IProduct> getSpecialty();

	@Override
	default List<IProduct> getSpecialties() {
		return getSpecialty();
	}

	/**
	 * Returns all drops of this block if you harvest it.
	 *
	 * @param genome       The genome of the tree of the leaves / pod.
	 * @param ripeningTime The ripening time of the block. From 0 to {@link #getRipeningPeriod}.
	 */
	List<ItemStack> getFruits(IGenome genome, Level level, int ripeningTime);

	/**
	 * @param ripeningTime Elapsed ripening time for the fruit.
	 * @return ResourceLocation of the texture to overlay on the leaf block.
	 */
	@Nullable
	ResourceLocation getSprite(IGenome genome, BlockGetter level, BlockPos pos, int ripeningTime);

	@Nullable
	default ResourceLocation getDecorativeSprite() {
		return null;
	}
}
