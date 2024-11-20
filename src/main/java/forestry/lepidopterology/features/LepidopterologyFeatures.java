package forestry.lepidopterology.features;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.lepidopterology.worldgen.CocoonDecorator;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

// todo this is not actually used anywhere. does anyone want this?
public class LepidopterologyFeatures {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	private static final DeferredRegister<Feature<?>> FEATURES = REGISTRY.getRegistry(Registries.FEATURE);
	private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = REGISTRY.getRegistry(Registries.CONFIGURED_FEATURE);
	private static final DeferredRegister<PlacedFeature> PLACED_FEATURES = REGISTRY.getRegistry(Registries.PLACED_FEATURE);

	public static final RegistryObject<CocoonDecorator> COCOON_DECORATOR = FEATURES.register("cocoon", CocoonDecorator::new);
	public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_COCOON_DECORATOR = CONFIGURED_FEATURES.register("cocoon", () -> new ConfiguredFeature<>(COCOON_DECORATOR.get(), FeatureConfiguration.NONE));
	public static final RegistryObject<?> PLACED_COCOON_DECORATOR = PLACED_FEATURES.register("cocoon", () -> new PlacedFeature(CONFIGURED_COCOON_DECORATOR.getHolder().get(), List.of()));
}
