package forestry.modules.features;

import forestry.api.ForestryConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class FeatureBlock<B extends Block, I extends BlockItem> extends ModFeature implements IBlockFeature<B, I> {
	private final RegistryObject<B> blockObject;
	@Nullable
	private final RegistryObject<I> itemObject;

	public FeatureBlock(IFeatureRegistry features, ResourceLocation moduleId, String identifier, Supplier<B> constructorBlock, @Nullable Function<B, I> constructorItem) {
		super(moduleId, identifier);
		this.blockObject = features.getRegistry(Registries.BLOCK).register(identifier, constructorBlock);
		this.itemObject = constructorItem == null ? null : features.getRegistry(Registries.ITEM).register(identifier, () -> constructorItem.apply(this.blockObject.get()));
	}

	public String getTranslationKey() {
		return this.blockObject.map(Block::getDescriptionId).orElseGet(() -> "block." + ForestryConstants.MOD_ID + "." + this.name.replace('/', '.'));
	}

	@Override
	public BlockState defaultState() {
		return block().defaultBlockState();
	}

	@Override
	public <V extends Comparable<V>> BlockState setValue(Property<V> property, V value) {
		return defaultState().setValue(property, value);
	}

	@Override
	public B block() {
		return this.blockObject.get();
	}

	@Override
	public I item() {
		return Objects.requireNonNull(this.itemObject, () -> "Missing item for block: " + this.name).get();
	}

	@Override
	public ResourceLocation id() {
		return this.itemObject.getId();
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.BLOCK;
	}
}
