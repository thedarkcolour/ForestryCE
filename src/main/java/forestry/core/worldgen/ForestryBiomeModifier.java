package forestry.core.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.IForestryApi;
import forestry.api.apiculture.hives.IHive;
import forestry.api.climate.IClimateManager;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.core.config.ForestryConfig;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

// Pass in the feature holders from the codec
public record ForestryBiomeModifier(Holder<PlacedFeature> hive, Holder<PlacedFeature> tree,
									Holder<PlacedFeature> apatiteOre,
									Holder<PlacedFeature> tinOre) implements BiomeModifier {
	public static final MapCodec<ForestryBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PlacedFeature.CODEC.fieldOf("hive").forGetter(ForestryBiomeModifier::hive),
		PlacedFeature.CODEC.fieldOf("tree").forGetter(ForestryBiomeModifier::tree),
		PlacedFeature.CODEC.fieldOf("apatite_ore").forGetter(ForestryBiomeModifier::apatiteOre),
		PlacedFeature.CODEC.fieldOf("tin_ore").forGetter(ForestryBiomeModifier::tinOre)
	).apply(instance, ForestryBiomeModifier::new));

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD) {
			// server configs are loaded, so ores can be added
			if (biome.is(BiomeTags.IS_OVERWORLD)) {
				if (ForestryConfig.SERVER.spawnTinOre.get()) {
					builder.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, this.tinOre);
				}
				if (ForestryConfig.SERVER.spawnApatiteOre.get()) {
					builder.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, this.apatiteOre);
				}
			}

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
	public MapCodec<? extends BiomeModifier> codec() {
		return CODEC;
	}
}
