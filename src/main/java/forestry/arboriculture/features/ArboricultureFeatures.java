package forestry.arboriculture.features;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.worldgen.TreeDecorator;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

//@FeatureProvider
public class ArboricultureFeatures {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final DeferredRegister<Feature<?>> FEATURES = REGISTRY.getRegistry(Registries.FEATURE);
	public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = REGISTRY.getRegistry(Registries.CONFIGURED_FEATURE);
	public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = REGISTRY.getRegistry(Registries.PLACED_FEATURE);

	public static final RegistryObject<TreeDecorator> TREE_DECORATOR = FEATURES.register("tree", TreeDecorator::new);
	public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_TREE_DECORATOR = CONFIGURED_FEATURES.register("tree", () -> new ConfiguredFeature<>(TREE_DECORATOR.get(), FeatureConfiguration.NONE));
	public static final RegistryObject<PlacedFeature> PLACED_TREE_DECORATOR = PLACED_FEATURES.register("tree", () -> new PlacedFeature(CONFIGURED_TREE_DECORATOR.getHolder().get(), List.of()));
}
