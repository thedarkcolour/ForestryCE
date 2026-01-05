package forestry.core.commands;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpecies;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Collection;

public interface IStatsSaveHelper {
	String getTranslationKey();

	void addExtraInfo(Collection<Component> statistics, IBreedingTracker breedingTracker);

	Collection<? extends ISpecies<?>> getSpecies();

	String getFileSuffix();

	IBreedingTracker getBreedingTracker(Level world, GameProfile gameProfile);
}
