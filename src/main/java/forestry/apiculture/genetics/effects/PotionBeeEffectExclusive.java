package forestry.apiculture.genetics.effects;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public class PotionBeeEffectExclusive extends PotionBeeEffect {
	private final Holder<MobEffect> exclude;

	public PotionBeeEffectExclusive(boolean dominant, Holder<MobEffect> potion, int duration, int throttle, float chance, Holder<MobEffect> exclude) {
		super(dominant, potion, duration, throttle, chance);
		this.exclude = exclude;
	}

	@Override
	public boolean secondaryEntityCheck(LivingEntity entity) {
		return !entity.hasEffect(this.exclude);
	}
}
