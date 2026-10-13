package forestry.apiculture.features;

import com.mojang.serialization.MapCodec;
import forestry.api.ForestryRegistries;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.bees.genetics.RequiresResourceBeeJubilance;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class ApicultureBeeJubilanceTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final DeferredRegister<MapCodec<? extends IBeeJubilance>> BEE_JUBILANCE_TYPES = REGISTRY.getRegistry(ForestryRegistries.Keys.BEE_JUBILANCE_TYPE);

	public static final DeferredHolder<MapCodec<? extends IBeeJubilance>, MapCodec<RequiresResourceBeeJubilance>> REQUIRES_RESOURCE = BEE_JUBILANCE_TYPES.register("requires_resource", () -> RequiresResourceBeeJubilance.MAP_CODEC);
}
