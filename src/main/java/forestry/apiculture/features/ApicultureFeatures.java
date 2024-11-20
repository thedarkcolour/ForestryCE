package forestry.apiculture.features;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.hives.HiveDecorator;
import forestry.core.worldgen.ApiaristPoolElement;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

//@FeatureProvider
public class ApicultureFeatures {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final DeferredRegister<Feature<?>> FEATURES = REGISTRY.getRegistry(Registries.FEATURE);
	public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = REGISTRY.getRegistry(Registries.CONFIGURED_FEATURE);
	public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = REGISTRY.getRegistry(Registries.PLACED_FEATURE);
	public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES = REGISTRY.getRegistry(Registries.STRUCTURE_POOL_ELEMENT);

	public static final RegistryObject<HiveDecorator> HIVE_DECORATOR = FEATURES.register("hive", HiveDecorator::new);
	public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_HIVE_DECORATOR = CONFIGURED_FEATURES.register("hive", () -> new ConfiguredFeature<>(HIVE_DECORATOR.get(), FeatureConfiguration.NONE));
	public static final RegistryObject<PlacedFeature> PLACED_HIVE_DECORATOR = PLACED_FEATURES.register("hive", () -> new PlacedFeature(CONFIGURED_HIVE_DECORATOR.getHolder().get(), List.of()));

	public static final RegistryObject<StructurePoolElementType<ApiaristPoolElement>> APIARIST_POOL_ELEMENT_TYPE = POOL_ELEMENT_TYPES.register("apiarist", () -> () -> ApiaristPoolElement.CODEC);
}
