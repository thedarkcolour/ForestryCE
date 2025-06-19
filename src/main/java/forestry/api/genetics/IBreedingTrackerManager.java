package forestry.api.genetics;

import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

public interface IBreedingTrackerManager {

	/**
	 * @param type    The ID of the species type.
	 * @param level   The level where this breeding tracker is saved.
	 * @param profile The profile of the player whose breeding tracker should be queried.
	 * @return The player-specific species tracker for the species type with the given ID.
	 */
	<T extends IBreedingTracker> T getTracker(ISpeciesType<?, ?> type, LevelAccessor level, @Nullable ResolvableProfile profile);
}
