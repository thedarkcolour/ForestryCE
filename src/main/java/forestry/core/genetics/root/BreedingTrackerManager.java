package forestry.core.genetics.root;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IBreedingTrackerManager;
import forestry.api.genetics.ISpeciesType;
import forestry.core.ClientsideCode;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;

public enum BreedingTrackerManager implements IBreedingTrackerManager {
	INSTANCE;

	private static final SidedHandler BREEDING_HANDLER = FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.newBreedingHandler() : new ServerBreedingHandler();

	@Override
	public <T extends IBreedingTracker> T getTracker(ISpeciesType<?, ?> type, LevelAccessor level, @Nullable ResolvableProfile profile) {
		return BREEDING_HANDLER.getTracker(type, level, profile);
	}

	interface SidedHandler {
		<T extends IBreedingTracker> T getTracker(ISpeciesType<?, ?> type, LevelAccessor level, @Nullable ResolvableProfile profile);
	}
}
