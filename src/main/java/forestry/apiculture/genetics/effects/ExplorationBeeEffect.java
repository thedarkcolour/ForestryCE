package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ExplorationBeeEffect extends ThrottledBeeEffect {

	public ExplorationBeeEffect() {
		super(false, 80, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<Player> players = ThrottledBeeEffect.getEntitiesInRange(genome, housing, Player.class);
		for (Player player : players) {
			player.giveExperiencePoints(2);
		}

		return storedData;
	}

}
