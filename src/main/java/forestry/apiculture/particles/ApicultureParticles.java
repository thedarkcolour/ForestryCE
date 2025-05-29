package forestry.apiculture.particles;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class ApicultureParticles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = REGISTRY.getRegistry(Registries.PARTICLE_TYPE);

	public static final DeferredHolder<ParticleType<?>, BeeParticleData.Type> BEE_EXPLORER_PARTICLE = PARTICLE_TYPES.register("bee_explore_particle", BeeParticleData.Type::new);
	public static final DeferredHolder<ParticleType<?>, BeeParticleData.Type> BEE_ROUND_TRIP_PARTICLE = PARTICLE_TYPES.register("bee_round_trip_particle", BeeParticleData.Type::new);
	public static final DeferredHolder<ParticleType<?>, BeeTargetParticleData.Type> BEE_TARGET_ENTITY_PARTICLE = PARTICLE_TYPES.register("bee_target_entity_particle", BeeTargetParticleData.Type::new);
}
