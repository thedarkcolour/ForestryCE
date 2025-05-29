package forestry.arboriculture.commands;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpecies;
import forestry.core.commands.IStatsSaveHelper;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

public class TreeStatsSaveHelper implements IStatsSaveHelper {
	@Override
	public String getTranslationKey() {
		return "for.chat.command.forestry.tree.save.stats";
	}

	@Override
	public void addExtraInfo(Collection<Component> statistics, IBreedingTracker breedingTracker) {
	}

	@Override
	public List<? extends ISpecies<?>> getSpecies() {
		return SpeciesUtil.getAllTreeSpecies();
	}

	@Override
	public String getFileSuffix() {
		return "trees";
	}

	@Override
	public IBreedingTracker getBreedingTracker(Level world, GameProfile gameProfile) {
		return SpeciesUtil.TREE_TYPE.get().getBreedingTracker(world, gameProfile);
	}

}
