package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class CorePaintings {
	private static final DeferredRegister<PaintingVariant> PAINTINGS = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(Registries.PAINTING_VARIANT);

	public static final DeferredHolder<PaintingVariant, PaintingVariant> MOUSETREE = PAINTINGS.register("mousetree", () -> new PaintingVariant(48, 48, CorePaintings.MOUSETREE.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> WASPHOL = PAINTINGS.register("wasphol", () -> new PaintingVariant(32, 32, CorePaintings.WASPHOL.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> CAGE = PAINTINGS.register("cage", () -> new PaintingVariant(32, 32, CorePaintings.CAGE.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> LEWIS = PAINTINGS.register("lewis", () -> new PaintingVariant(32, 48, CorePaintings.LEWIS.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> SITEBEE = PAINTINGS.register("site_bee", () -> new PaintingVariant(32, 32, CorePaintings.SITEBEE.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> ALEXBLOOME = PAINTINGS.register("alex_bloome", () -> new PaintingVariant(64, 32, CorePaintings.ALEXBLOOME.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> SUSPICIOUS_LOOKING_TREE = PAINTINGS.register("suspicious_looking_tree", () -> new PaintingVariant(32, 48, CorePaintings.SUSPICIOUS_LOOKING_TREE.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> WISDOM = PAINTINGS.register("wisdom", () -> new PaintingVariant(32, 32, CorePaintings.WISDOM.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> MYSTICAL_TREE = PAINTINGS.register("mystical_tree", () -> new PaintingVariant(32, 32, CorePaintings.MYSTICAL_TREE.getId()));
	public static final DeferredHolder<PaintingVariant, PaintingVariant> DEKU = PAINTINGS.register("deku", () -> new PaintingVariant(64, 32, CorePaintings.DEKU.getId()));
}
