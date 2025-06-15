package forestry.api.apiculture.bee;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

public interface IBeeSpeciesType extends ISpeciesType<IBeeSpecies, IBee> {
	/**
	 * @return {@link IApiaristTracker} associated with the passed world.
	 */
	@Override
	IApiaristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile);

	/**
	 * @return true if passed item is a drone. Equal to getLifeStage(ItemStack stack) == EnumBeeType.DRONE
	 */
	boolean isDrone(ItemStack stack);

	/**
	 * @return true if passed item is mated (i.e. a queen)
	 */
	boolean isMated(ItemStack stack);
}
