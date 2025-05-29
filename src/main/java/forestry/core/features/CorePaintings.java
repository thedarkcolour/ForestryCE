package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@FeatureProvider
public class CorePaintings {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);
	private static final DeferredRegister<PaintingVariant> PAINTINGS = REGISTRY.getRegistry(Registries.PAINTING_VARIANT);

	public static final RegistryObject<PaintingVariant> MOUSETREE = PAINTINGS.register("mousetree", () -> new PaintingVariant(48, 48));
	public static final RegistryObject<PaintingVariant> WASPHOL = PAINTINGS.register("wasphol", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> CAGE = PAINTINGS.register("cage", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> LEWIS = PAINTINGS.register("lewis", () -> new PaintingVariant(32, 48));
	public static final RegistryObject<PaintingVariant> SITEBEE = PAINTINGS.register("site_bee", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> ALEXBLOOME = PAINTINGS.register("alex_bloome", () -> new PaintingVariant(64, 32));
	public static final RegistryObject<PaintingVariant> SUSPICIOUS_LOOKING_TREE = PAINTINGS.register("suspicious_looking_tree", () -> new PaintingVariant(32, 48));
	public static final RegistryObject<PaintingVariant> WISDOM = PAINTINGS.register("wisdom", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> MYSTICAL_TREE = PAINTINGS.register("mystical_tree", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> DEKU = PAINTINGS.register("deku", () -> new PaintingVariant(64, 32));
}
