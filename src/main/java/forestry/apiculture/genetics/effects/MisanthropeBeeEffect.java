package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.core.damage.CoreDamageTypes;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class MisanthropeBeeEffect extends ThrottledBeeEffect {
	public MisanthropeBeeEffect() {
		super(true, 20, false, false);
	}

	@Override
	public IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<Player> players = ThrottledBeeEffect.getEntitiesInRange(genome, housing, Player.class);
		for (Player player : players) {
			int damage = 4;

			// Entities are not attacked if they wear a full set of apiarist's armor.
			int count = BeeManager.armorApiaristHelper.wearsItems(player, this, true);
			damage -= count;
			if (damage <= 0) {
				continue;
			}

			player.hurt(CoreDamageTypes.source(housing.getWorldObj(), CoreDamageTypes.MISANTHROPE), damage);
		}

		return storedData;
	}

}
