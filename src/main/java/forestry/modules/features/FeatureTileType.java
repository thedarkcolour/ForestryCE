package forestry.modules.features;

import java.util.Collection;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraftforge.registries.RegistryObject;

public class FeatureTileType<T extends BlockEntity> extends ModFeature implements ITileTypeFeature<T> {
	private final RegistryObject<BlockEntityType<T>> blockEntityObject;

	public FeatureTileType(IFeatureRegistry registry, ResourceLocation moduleId, String name, BlockEntityType.BlockEntitySupplier<T> constructorTileEntity, Supplier<Collection<? extends Block>> validBlocks) {
		super(moduleId, name);
		this.blockEntityObject = registry.getRegistry(Registries.BLOCK_ENTITY_TYPE).register(name, () -> BlockEntityType.Builder.of(constructorTileEntity, validBlocks.get().toArray(Block[]::new)).build(null));
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.BLOCK_ENTITY_TYPE;
	}

	@Override
	public BlockEntityType<T> tileType() {
		return blockEntityObject.get();
	}
}
