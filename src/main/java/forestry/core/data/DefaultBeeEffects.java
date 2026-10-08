package forestry.core.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;

import forestry.api.apiculture.ForestryBeeEffects;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.TemperatureType;
import forestry.apiculture.bees.genetics.ApicultureReloadHandler;
import forestry.apiculture.bees.genetics.BlockMatcher;
import forestry.apiculture.bees.genetics.effects.AgingBeeEffect;
import forestry.apiculture.bees.genetics.effects.DamageBeeEffect;
import forestry.apiculture.bees.genetics.effects.PotionBeeEffect;
import forestry.apiculture.bees.genetics.effects.ResurrectionBeeEffect;
import forestry.apiculture.bees.genetics.effects.ThrottleSettings;
import forestry.apiculture.bees.genetics.effects.TransformBlockBeeEffect;
import forestry.core.platform.damage.CoreDamageTypes;

public class DefaultBeeEffects {
	public static Map<ResourceLocation, IBeeEffect> create() {
		Map<ResourceLocation, IBeeEffect> effects = new LinkedHashMap<>();

		effects.put(ForestryBeeEffects.BEATIFIC, new PotionBeeEffect(false, MobEffects.REGENERATION, 100));
		effects.put(ForestryBeeEffects.MIASMIC, new PotionBeeEffect(false, MobEffects.POISON, 600, 100, 0.1f));
		effects.put(ForestryBeeEffects.DRUNKARD, new PotionBeeEffect(false, MobEffects.CONFUSION, 100));
		effects.put(ForestryBeeEffects.DARKNESS, new PotionBeeEffect(false, MobEffects.DARKNESS, 150));
		effects.put(ForestryBeeEffects.REANIMATION, new ResurrectionBeeEffect(true, 40, ResurrectionBeeEffect.getReanimationList()));
		effects.put(ForestryBeeEffects.RESURRECTION, new ResurrectionBeeEffect(true, 40, ResurrectionBeeEffect.getResurrectionList()));
		effects.put(ForestryBeeEffects.REJUVENATION, new AgingBeeEffect(false, false));
		effects.put(ForestryBeeEffects.CHRONOPHAGE, new AgingBeeEffect(false, true));
		effects.put(ForestryBeeEffects.AGGRESSIVE, new DamageBeeEffect(new ThrottleSettings(true, 40, false, false), 4f, true, 1.0f, CoreDamageTypes.AGGRESSIVE, DamageBeeEffect.Target.Builtin.ALL));
		effects.put(ForestryBeeEffects.MISANTHROPE, new DamageBeeEffect(new ThrottleSettings(true, 20, false, false), 4f, true, 1.0f, CoreDamageTypes.MISANTHROPE, DamageBeeEffect.Target.Builtin.PLAYERS));
		effects.put(ForestryBeeEffects.HEROIC, new DamageBeeEffect(new ThrottleSettings(false, 40, true, false), 2f, false, 1.0f, CoreDamageTypes.HEROIC, DamageBeeEffect.Target.Builtin.MONSTERS));
		effects.put(ForestryBeeEffects.SIFTER, new TransformBlockBeeEffect(
			new ThrottleSettings(true, 550, true, true),
			List.of(new TransformBlockBeeEffect.Transform(
				new BlockMatcher.Tag(BlockTags.DIRT),
				new TransformBlockBeeEffect.To.Fixed(Blocks.COARSE_DIRT.defaultBlockState()),
				false)),
			1, 1.0f, Optional.empty()));
		// max_temperature is inclusive, so NORMAL skips WARM and warmer
		effects.put(ForestryBeeEffects.GLACIAL, new TransformBlockBeeEffect(
			new ThrottleSettings(false, 200, true, false),
			List.of(new TransformBlockBeeEffect.Transform(
				new BlockMatcher.Direct(List.of(Blocks.WATER)),
				new TransformBlockBeeEffect.To.Fixed(Blocks.ICE.defaultBlockState()),
				true)),
			10, 1.0f, Optional.of(TemperatureType.NORMAL)));
		// a tag and not the BERRIES property, which also matched modded blocks by accident
		effects.put(ForestryBeeEffects.GLOW_BERRY_GROW, new TransformBlockBeeEffect(
			new ThrottleSettings(false, 200, true, true),
			List.of(new TransformBlockBeeEffect.Transform(
				new BlockMatcher.Tag(BlockTags.CAVE_VINES),
				new TransformBlockBeeEffect.To.SetProperties(Map.of("berries", "true")),
				false)),
			1, 1.0f, Optional.empty()));

		return effects;
	}

	// a data generator run never fires the datapack reload that loads effects
	public static void seedLiveBeeEffectsForDatagen() {
		ApicultureReloadHandler.rebuildBeeEffects(create());
	}
}
