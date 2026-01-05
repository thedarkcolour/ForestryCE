package forestry.apiculture.commands;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.IBreedingTracker;
import forestry.core.commands.IStatsSaveHelper;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class BeeStatsSaveHelper implements IStatsSaveHelper {
	@Override
	public String getTranslationKey() {
		return "for.chat.command.forestry.bee.save.stats";
	}

	@Override
	public void addExtraInfo(Collection<Component> statistics, IBreedingTracker breedingTracker) {
		IApiaristTracker tracker = (IApiaristTracker) breedingTracker;
		Component discoveredLine = Component.translatable("for.chat.command.forestry.stats.save.key.discovered").append(":");
		statistics.add(discoveredLine);
		// todo lines
		//statistics.add(StringUtil.line(discoveredLine.length()));

		MutableComponent queen = Component.translatable("for.bees.grammar.queen.type");
		MutableComponent princess = Component.translatable("for.bees.grammar.princess.type");
		MutableComponent drone = Component.translatable("for.bees.grammar.drone.type");
		statistics.add(queen.append(":\t\t" + tracker.getQueenCount()));
		// why does this one only have 1 tab?
		statistics.add(princess.append(":\t" + tracker.getPrincessCount()));
		statistics.add(drone.append(":\t\t" + tracker.getDroneCount()));
		statistics.add(Component.literal(""));
	}

	@Override
	public Collection<IBeeSpecies> getSpecies() {
		return SpeciesUtil.getAllBeeSpecies();
	}

	@Override
	public String getFileSuffix() {
		return "bees";
	}

	@Override
	public IBreedingTracker getBreedingTracker(Level level, GameProfile gameProfile) {
		return SpeciesUtil.BEE_TYPE.get().getBreedingTracker(level, gameProfile);
	}

}
