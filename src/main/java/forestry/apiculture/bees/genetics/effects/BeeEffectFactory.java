package forestry.apiculture.bees.genetics.effects;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeEffectFactory;

public class BeeEffectFactory implements IBeeEffectFactory {
	@Override
	public IBeeEffect createPotion(boolean dominant, Holder<MobEffect> effect, int duration) {
		return new PotionBeeEffect(dominant, effect, duration);
	}

	@Override
	public IBeeEffect createPotion(boolean dominant, Holder<MobEffect> effect, int duration, int throttle, float chance) {
		return new PotionBeeEffect(dominant, effect, duration, throttle, chance);
	}
}
