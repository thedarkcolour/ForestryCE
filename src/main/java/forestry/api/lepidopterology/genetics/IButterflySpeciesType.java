package forestry.api.lepidopterology.genetics;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

public interface IButterflySpeciesType extends ISpeciesType<IButterflySpecies, IButterfly> {
	@Override
	IBreedingTracker getBreedingTracker(LevelAccessor level, @Nullable @Nullable ResolvableProfile profile);

	/**
	 * Spawns the given butterfly in the world.
	 *
	 * @return butterfly entity on success, null otherwise.
	 */
	Mob spawnButterflyInWorld(Level level, IButterfly butterfly, double x, double y, double z);

	@Nullable
	BlockPos plantCocoon(LevelAccessor level, BlockPos pos, IButterfly caterpillar, int age, boolean createNursery);

	/**
	 * @return true if passed item is mated.
	 */
	boolean isMated(ItemStack stack);
}
