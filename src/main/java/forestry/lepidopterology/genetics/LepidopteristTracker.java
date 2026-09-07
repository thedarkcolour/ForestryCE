package forestry.lepidopterology.genetics;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.ISpecies;
import forestry.api.lepidopterology.ILepidopteristTracker;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.core.genetics.BreedingTracker;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class LepidopteristTracker extends BreedingTracker implements ILepidopteristTracker {
	public LepidopteristTracker() {
		super(ForestrySpeciesTypes.BUTTERFLY);
	}

	@Override
	public void registerCatch(IButterfly butterfly) {
		registerSpecies(butterfly.getSpecies());
		registerSpecies(butterfly.getInactiveSpecies());
	}

	@Override
	public void registerPickup(ISpecies<?> species) {
		registerSpecies(species);
	}

	@Override
	public void registerProgress(@Nullable Level level, @Nullable GameProfile profile, ISpecies<?> species) {
		//Lol butterflies suck imagine tracking their research progress
	}
}
