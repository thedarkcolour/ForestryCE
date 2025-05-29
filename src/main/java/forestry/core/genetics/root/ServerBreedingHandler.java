package forestry.core.genetics.root;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;

public class ServerBreedingHandler implements BreedingTrackerManager.SidedHandler {
	@Override
	@SuppressWarnings("unchecked")
	public <T extends IBreedingTracker> T getTracker(ISpeciesType<?, ?> type, LevelAccessor level, @Nullable GameProfile profile) {
		String filename = type.getBreedingTrackerFile(profile);
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
		T tracker = (T) overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> (SavedData) type.createBreedingTracker(), (nbt, registries) -> (SavedData) type.createBreedingTracker(nbt, registries)), filename);
		type.initializeBreedingTracker(tracker, overworld, profile);
		return tracker;
	}
}
