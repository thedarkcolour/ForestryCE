package forestry.core.particles;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@FeatureProvider
public class CoreParticles {
	private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE).getRegistry(Registries.PARTICLE_TYPE);

	public static final RegistryObject<SimpleParticleType> REFRACTORY_WAX = PARTICLE_TYPES.register("refractory_wax", () -> new SimpleParticleType(true));
}
