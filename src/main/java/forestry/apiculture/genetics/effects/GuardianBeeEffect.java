package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class GuardianBeeEffect extends ThrottledBeeEffect {
	public GuardianBeeEffect() {
		super(true, 1200, true, true);
	}

	@Override
	IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		List<Player> list = getEntitiesInRange(genome, housing, Player.class);
		for (Player player : list) {
			if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
				int count = BeeManager.armorApiaristHelper.wearsItems(player, this, true);
				if (count >= 4) {
					continue;
				}
				player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 6000 - 1500 * count, 2));
				((ServerPlayer) player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1F));
			}
		}
		return storedData;
	}
}
