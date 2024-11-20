package forestry.core.features;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.worldgen.ForestryBiomeModifier;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CoreFeatures {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = REGISTRY.getRegistry(Registries.CONFIGURED_FEATURE);
	private static final DeferredRegister<PlacedFeature> PLACED_FEATURES = REGISTRY.getRegistry(Registries.PLACED_FEATURE);
	private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = REGISTRY.getRegistry(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS);

	private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_APATITE = CONFIGURED_FEATURES.register("ore_apatite",
			() -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
					OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), CoreBlocks.APATITE_ORE.defaultState()),
					OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), CoreBlocks.DEEPSLATE_APATITE_ORE.defaultState())
			), 9)));

	private static final RegistryObject<ConfiguredFeature<?, ?>> ORE_TIN = CONFIGURED_FEATURES.register("ore_tin",
			() -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
					OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), CoreBlocks.TIN_ORE.defaultState()),
					OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), CoreBlocks.DEEPSLATE_TIN_ORE.defaultState())
			), 9)));

	private static final RegistryObject<PlacedFeature> PLACED_APATITE = PLACED_FEATURES.register("ore_apatite",
			() -> new PlacedFeature(ORE_APATITE.getHolder().get(), OrePlacements.commonOrePlacement(3, HeightRangePlacement.triangle(
					VerticalAnchor.absolute(48),
					VerticalAnchor.absolute(112)
			))));

	private static final RegistryObject<PlacedFeature> PLACED_TIN = PLACED_FEATURES.register("ore_tin",
			() -> new PlacedFeature(ORE_TIN.getHolder().get(), OrePlacements.commonOrePlacement(16, HeightRangePlacement.triangle(
					VerticalAnchor.bottom(), VerticalAnchor.absolute(64)
			))));

	// Responsible for hives + trees
	private static final RegistryObject<Codec<ForestryBiomeModifier>> FORESTRY = BIOME_MODIFIERS.register("forestry", () -> ForestryBiomeModifier.CODEC);
}
