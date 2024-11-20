package forestry.modules.features;

import java.util.Objects;
import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import net.minecraftforge.registries.RegistryObject;

public class FeatureCreativeTab extends ModFeature {
	private final RegistryObject<CreativeModeTab> creativeTabObject;

	public FeatureCreativeTab(IFeatureRegistry registry, ResourceLocation moduleId, String name, Consumer<CreativeModeTab.Builder> builder) {
		super(moduleId, name);

		this.creativeTabObject = registry.getRegistry(Registries.CREATIVE_MODE_TAB).register(name, () -> {
			CreativeModeTab.Builder tab = CreativeModeTab.builder().title(Component.translatable("itemGroup." + name));
			builder.accept(tab);
			return tab.build();
		});
	}

	public CreativeModeTab creativeTab() {
		return this.creativeTabObject.get();
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.CREATIVE_MODE_TAB;
	}

	public ResourceKey<CreativeModeTab> getKey() {
		return Objects.requireNonNull(this.creativeTabObject.getKey());
	}
}
