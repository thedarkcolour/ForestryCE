package forestry.core.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import forestry.api.ForestryConstants;
import forestry.apiculture.features.ApicultureFeatures;
import forestry.arboriculture.features.ArboricultureFeatures;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreFeatures;

public class ForestryFeaturesProvider extends DatapackBuiltinEntriesProvider {
	public ForestryFeaturesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, new RegistrySetBuilder()
				.add(Registries.CONFIGURED_FEATURE, ForestryFeaturesProvider::addConfiguredFeatures)
				.add(Registries.PLACED_FEATURE, ForestryFeaturesProvider::addPlacedFeatures), Set.of(ForestryConstants.MOD_ID));
	}

	private static void addConfiguredFeatures(BootstapContext<ConfiguredFeature<?, ?>> context) {
		context.register(CoreFeatures.ORE_APATITE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
				OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), CoreBlocks.APATITE_ORE.defaultState()),
				OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), CoreBlocks.DEEPSLATE_APATITE_ORE.defaultState())
		), 9)));
		context.register(CoreFeatures.ORE_TIN, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
				OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), CoreBlocks.TIN_ORE.defaultState()),
				OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), CoreBlocks.DEEPSLATE_TIN_ORE.defaultState())
		), 9)));

		context.register(ApicultureFeatures.CONFIGURED_HIVE, new ConfiguredFeature<>(ApicultureFeatures.HIVE.get(), FeatureConfiguration.NONE));

		context.register(ArboricultureFeatures.CONFIGURED_TREE, new ConfiguredFeature<>(ArboricultureFeatures.TREE_DECORATOR.get(), FeatureConfiguration.NONE));
	}

	private static void addPlacedFeatures(BootstapContext<PlacedFeature> context) {
		HolderGetter<ConfiguredFeature<?, ?>> holders = context.lookup(Registries.CONFIGURED_FEATURE);

		context.register(CoreFeatures.PLACED_APATITE, new PlacedFeature(holders.getOrThrow(CoreFeatures.ORE_APATITE), OrePlacements.commonOrePlacement(3, HeightRangePlacement.triangle(
				VerticalAnchor.absolute(48),
				VerticalAnchor.absolute(112)
		))));
		context.register(CoreFeatures.PLACED_TIN, new PlacedFeature(holders.getOrThrow(CoreFeatures.ORE_TIN), OrePlacements.commonOrePlacement(16, HeightRangePlacement.triangle(
				VerticalAnchor.bottom(), VerticalAnchor.absolute(64)
		))));

		context.register(ApicultureFeatures.PLACEED_HIVE, new PlacedFeature(holders.getOrThrow(ApicultureFeatures.CONFIGURED_HIVE), List.of()));

		context.register(ArboricultureFeatures.PLACED_TREE, new PlacedFeature(holders.getOrThrow(ArboricultureFeatures.CONFIGURED_TREE), List.of()));
	}
}
