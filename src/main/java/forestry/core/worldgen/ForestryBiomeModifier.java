package forestry.core.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import forestry.api.IForestryApi;
import forestry.api.apiculture.hives.IHive;
import forestry.api.climate.IClimateManager;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;

public record ForestryBiomeModifier(Holder<PlacedFeature> hive, Holder<PlacedFeature> tree) implements BiomeModifier {
	public static final Codec<ForestryBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlacedFeature.CODEC.fieldOf("hive").forGetter(ForestryBiomeModifier::hive),
			PlacedFeature.CODEC.fieldOf("tree").forGetter(ForestryBiomeModifier::tree)
	).apply(instance, ForestryBiomeModifier::new));

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD) {
			IClimateManager climates = IForestryApi.INSTANCE.getClimateManager();
			TemperatureType temperature = climates.getTemperature(biome);
			HumidityType humidity = climates.getHumidity(biome);

			builder.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, this.tree);

			for (IHive hive : IForestryApi.INSTANCE.getHiveManager().getHives()) {
				if (hive.isGoodBiome(biome) && hive.isGoodTemperature(temperature) && hive.isGoodHumidity(humidity)) {
					builder.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, this.hive);
					return;
				}
			}
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return CODEC;
	}
}
