package forestry.core.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CoreDamageTypes {
	private static final DeferredRegister<DamageType> REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(Registries.DAMAGE_TYPE);

	public static final RegistryObject<DamageType> AGGRESSIVE = REGISTRY.register("aggressive", () -> new DamageType("forestry.aggressive", 0.0f));
	public static final RegistryObject<DamageType> HEROIC = REGISTRY.register("heroic", () -> new DamageType("forestry.heroic", 0.0f));
	public static final RegistryObject<DamageType> MISANTHROPE = REGISTRY.register("misanthrope", () -> new DamageType("forestry.misanthrope", 0.0f));
	public static final RegistryObject<DamageType> RADIOACTIVE = REGISTRY.register("radioactive", () -> new DamageType("forestry.radioactive", 0.0f));
	public static final RegistryObject<DamageType> HIVE = REGISTRY.register("hive", () -> new DamageType("forestry.hive", 0.0f));
	public static final RegistryObject<DamageType> CLOCKWORK = REGISTRY.register("clockwork", () -> new DamageType("forestry.clockwork", 0.0f));
}
