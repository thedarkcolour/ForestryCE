package forestry.core.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.worldgen.ForestryBiomeModifier;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CoreFeatures {
	private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS);

	public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_APATITE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ForestryConstants.forestry("ore_apatite"));
	public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_TIN = ResourceKey.create(Registries.CONFIGURED_FEATURE, ForestryConstants.forestry("ore_tin"));

	public static final ResourceKey<PlacedFeature> PLACED_APATITE = ResourceKey.create(Registries.PLACED_FEATURE, ForestryConstants.forestry("ore_apatite"));
	public static final ResourceKey<PlacedFeature> PLACED_TIN = ResourceKey.create(Registries.PLACED_FEATURE, ForestryConstants.forestry("ore_tin"));

	// Responsible for hives + trees
	private static final RegistryObject<Codec<ForestryBiomeModifier>> FORESTRY = BIOME_MODIFIERS.register("forestry", () -> ForestryBiomeModifier.CODEC);
}
