package forestry.modules.features;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class FeatureItem<I extends Item> extends ModFeature implements IItemFeature<I> {
	private final RegistryObject<I> itemObject;

	public FeatureItem(IFeatureRegistry registry, ResourceLocation moduleId, String identifier, Supplier<I> constructor) {
		super(moduleId, identifier);
		this.itemObject = registry.getRegistry(Registries.ITEM).register(identifier, constructor);
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.ITEM;
	}

	@Override
	public I item() {
		return this.itemObject.get();
	}

	@Override
	public ResourceLocation id() {
		return this.itemObject.getId();
	}
}
