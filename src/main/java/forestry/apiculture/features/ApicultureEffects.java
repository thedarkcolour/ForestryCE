package forestry.apiculture.features;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class ApicultureEffects {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	private static final DeferredRegister<MobEffect> MOB_EFFECTS = REGISTRY.getRegistry(Registries.MOB_EFFECT);

	public static final Holder<MobEffect> HAKUNA_MATATA = MOB_EFFECTS.register("hakuna_matata", () -> {
		return new ForestryMobEffect(MobEffectCategory.BENEFICIAL, 0x069af3)
			.addAttributeModifier(Attributes.FOLLOW_RANGE, ForestryConstants.forestry("effect.hakuna_matata"), 0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	});
	public static final Holder<MobEffect> MATATA = MOB_EFFECTS.register("matata", () -> {
		return new ForestryMobEffect(MobEffectCategory.NEUTRAL, 0x380835);
	});

	public static class ForestryMobEffect extends MobEffect {
		protected ForestryMobEffect(MobEffectCategory category, int color) {
			super(category, color);
		}

		// we have no ongoing effects
		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
			return false;
		}
	}
}
