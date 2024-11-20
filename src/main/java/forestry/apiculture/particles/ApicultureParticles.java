package forestry.apiculture.particles;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;

import com.mojang.serialization.Codec;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class ApicultureParticles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = REGISTRY.getRegistry(Registries.PARTICLE_TYPE);

	public static final RegistryObject<ParticleType<BeeParticleData>> BEE_EXPLORER_PARTICLE = PARTICLE_TYPES.register("bee_explore_particle", BeeParticleType::new);
	public static final RegistryObject<ParticleType<BeeParticleData>> BEE_ROUND_TRIP_PARTICLE = PARTICLE_TYPES.register("bee_round_trip_particle", BeeParticleType::new);
	public static final RegistryObject<ParticleType<BeeTargetParticleData>> BEE_TARGET_ENTITY_PARTICLE = PARTICLE_TYPES.register("bee_target_entity_particle", () -> new ParticleType<>(false, BeeTargetParticleData.DESERIALIZER) {
		@Override
		public Codec<BeeTargetParticleData> codec() {
			return BeeTargetParticleData.CODEC;
		}
	});
}
