package forestry.core.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CorePaintings {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);
	private static final DeferredRegister<PaintingVariant> PAINTINGS = REGISTRY.getRegistry(Registries.PAINTING_VARIANT);

	public static final RegistryObject<PaintingVariant> MOUSETREE = PAINTINGS.register("mousetree", () -> new PaintingVariant(48, 48));
	public static final RegistryObject<PaintingVariant> WASPHOL = PAINTINGS.register("wasphol", () -> new PaintingVariant(32, 32));
}
