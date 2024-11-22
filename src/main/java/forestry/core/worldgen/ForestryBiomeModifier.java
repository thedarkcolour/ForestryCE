package forestry.core.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import forestry.api.IForestryApi;
import forestry.api.apiculture.hives.IHive;
import forestry.api.climate.IClimateManager;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.apiculture.features.ApicultureFeatures;
import forestry.arboriculture.features.ArboricultureFeatures;

public class ForestryBiomeModifier implements BiomeModifier {
	// should this be wrapped in Lazy singleton?
	public static final Codec<ForestryBiomeModifier> CODEC = Codec.unit(ForestryBiomeModifier::new);

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD) {
			IClimateManager climates = IForestryApi.INSTANCE.getClimateManager();
			TemperatureType temperature = climates.getTemperature(biome);
			HumidityType humidity = climates.getHumidity(biome);

			//builder.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArboricultureFeatures.PLACED_TREE_DECORATOR.getHolder().get());

			for (IHive hive : IForestryApi.INSTANCE.getHiveManager().getHives()) {
				if (hive.isGoodBiome(biome) && hive.isGoodTemperature(temperature) && hive.isGoodHumidity(humidity)) {
					//builder.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ApicultureFeatures.PLACED_HIVE_DECORATOR.getHolder().get());
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
