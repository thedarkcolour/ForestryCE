package forestry.modules.features;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FeatureMenuType<M extends AbstractContainerMenu> extends ModFeature implements IMenuTypeFeature<M> {
	private final DeferredHolder<MenuType<?>, MenuType<M>> menuTypeObject;

	public FeatureMenuType(IFeatureRegistry registry, ResourceLocation moduleId, String identifier, IContainerFactory<M> containerFactory) {
		super(moduleId, identifier);
		this.menuTypeObject = registry.getRegistry(Registries.MENU).register(identifier, () -> new MenuType<>(containerFactory, FeatureFlags.DEFAULT_FLAGS));
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.MENU;
	}

	@Override
	public MenuType<M> menuType() {
		return this.menuTypeObject.get();
	}
}
