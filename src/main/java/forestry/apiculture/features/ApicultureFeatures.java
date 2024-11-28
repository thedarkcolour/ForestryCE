package forestry.apiculture.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.hives.HiveDecorator;
import forestry.core.worldgen.ApiaristPoolElement;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class ApicultureFeatures {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final DeferredRegister<Feature<?>> FEATURES = REGISTRY.getRegistry(Registries.FEATURE);
	public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES = REGISTRY.getRegistry(Registries.STRUCTURE_POOL_ELEMENT);

	public static final RegistryObject<HiveDecorator> HIVE = FEATURES.register("hive", HiveDecorator::new);
	public static final RegistryObject<StructurePoolElementType<ApiaristPoolElement>> APIARIST_POOL_ELEMENT_TYPE = POOL_ELEMENT_TYPES.register("apiarist", () -> () -> ApiaristPoolElement.CODEC);

	public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_HIVE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ForestryConstants.forestry("hive"));
	public static final ResourceKey<PlacedFeature> PLACED_HIVE = ResourceKey.create(Registries.PLACED_FEATURE, ForestryConstants.forestry("hive"));
}
