package forestry.modules.features;

import com.google.common.collect.ArrayListMultimap;
import forestry.api.core.IBlockSubtype;
import forestry.api.core.IItemSubtype;
import forestry.core.utils.ModUtil;
import forestry.modules.ModuleUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;

public class ModFeatureRegistry {
	// Maps module id to feature (needed because of Binnie)
	private static final LinkedHashMap<String, ModFeatureRegistry> MOD_REGISTRY = new LinkedHashMap<>();

	private final HashMap<ResourceLocation, ModuleFeatureRegistry> modules = new LinkedHashMap<>();
	private final IEventBus modBus;

	private ModFeatureRegistry(String modId) {
		this.modBus = ModuleUtil.getModBus(modId);
	}

	public void register(IModFeature feature) {
		getRegistry(feature.getModuleId()).register(feature);
	}

	public static IFeatureRegistry get(ResourceLocation moduleId) {
		return MOD_REGISTRY.computeIfAbsent(moduleId.getNamespace(), ModFeatureRegistry::new).getRegistry(moduleId);
	}

	public static Map<String, ModFeatureRegistry> getRegistries() {
		return MOD_REGISTRY;
	}

	public IFeatureRegistry getRegistry(ResourceLocation moduleId) {
		return this.modules.computeIfAbsent(moduleId, key -> new ModuleFeatureRegistry(key, this.modBus));
	}

	public Map<ResourceLocation, IFeatureRegistry> getModules() {
		return Collections.unmodifiableMap(this.modules);
	}

	private static class ModuleFeatureRegistry implements IFeatureRegistry {
		private final ArrayList<IModFeature> features = new ArrayList<>();
		private final ArrayListMultimap<ResourceKey<? extends Registry<?>>, IModFeature> featureByRegistry = ArrayListMultimap.create();
		@SuppressWarnings("rawtypes")
		private final HashMap<ResourceKey, DeferredRegister> registries = new HashMap<>();

		private final ResourceLocation moduleId;
		private final IEventBus modBus;

		public ModuleFeatureRegistry(ResourceLocation moduleId, IEventBus modBus) {
			this.moduleId = moduleId;
			this.modBus = modBus;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V> DeferredRegister<V> getRegistry(ResourceKey<? extends Registry<V>> registryKey) {
			String modId = this.moduleId.getNamespace();
			return this.registries.computeIfAbsent(registryKey, key -> {
				@SuppressWarnings("rawtypes")
				DeferredRegister registry;

				if (registryKey.equals(Registries.BLOCK)) {
					registry = DeferredRegister.createBlocks(modId);
				} else if (registryKey.equals(Registries.ITEM)) {
					registry = DeferredRegister.createItems(modId);
				} else {
					registry = DeferredRegister.create(key, modId);
				}

				registry.register(this.modBus);
				return registry;
			});
		}

		@Override
		public DeferredRegister.Blocks getBlockRegistry() {
			return (DeferredRegister.Blocks) getRegistry(Registries.BLOCK);
		}

		@Override
		public DeferredRegister.Items getItemRegistry() {
			return (DeferredRegister.Items) getRegistry(Registries.ITEM);
		}

		@Nullable
		@Override
		@SuppressWarnings("unchecked")
		public <V> DeferredRegister<V> getRegistryNullable(ResourceKey<? extends Registry<V>> registry) {
			return this.registries.get(registry);
		}

		@Override
		public <B extends Block, I extends BlockItem> FeatureBlock<B, I> block(Supplier<B> constructor, String name) {
			return block(constructor, null, name);
		}

		@Override
		public <B extends Block, I extends BlockItem> FeatureBlock<B, I> block(Supplier<B> constructor, @Nullable Function<B, I> itemConstructor, String name) {
			return register(new FeatureBlock<>(this, this.moduleId, name, constructor, itemConstructor));
		}

		@Override
		public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(Function<S, B> constructor, Class<? extends S> typeClass) {
			return new FeatureBlockGroup.Builder<>(this, constructor);
		}

		@Override
		public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(Function<S, B> constructor, Collection<S> types) {
			return (FeatureBlockGroup.Builder<B, S>) new FeatureBlockGroup.Builder<>(this, constructor).types(types);
		}

		@Override
		public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(Function<S, B> constructor, S[] types) {
			return (FeatureBlockGroup.Builder<B, S>) new FeatureBlockGroup.Builder<>(this, constructor).types(types);
		}

		@Override
		public <I extends Item> FeatureItem<I> item(Supplier<I> constructor, String name) {
			return register(new FeatureItem<>(this, this.moduleId, name, constructor));
		}

		@Override
		public <I extends Item, S extends IItemSubtype> FeatureItemGroup<I, S> itemGroup(Function<S, I> constructor, String identifier, S[] subTypes) {
			return itemGroup(constructor, subTypes).identifier(identifier).create();
		}

		@Override
		public <I extends Item, S extends IItemSubtype> FeatureItemGroup.Builder<I, S> itemGroup(Function<S, I> constructor, S[] subTypes) {
			return (FeatureItemGroup.Builder<I, S>) new FeatureItemGroup.Builder<>(this, constructor).types(subTypes);
		}

		@Override
		public <I extends Item, R extends IItemSubtype, C extends IItemSubtype> FeatureItemTable<I, R, C> itemTable(BiFunction<R, C, I> constructor, R[] rowTypes, C[] columnTypes, String identifier) {
			return itemTable(constructor, rowTypes, columnTypes).identifier(identifier).create();
		}

		@Override
		public <I extends Item, R extends IItemSubtype, C extends IItemSubtype> FeatureItemTable.Builder<I, R, C> itemTable(BiFunction<R, C, I> constructor, R[] rowTypes, C[] columnTypes) {
			return (FeatureItemTable.Builder<I, R, C>) new FeatureItemTable.Builder<>(this, constructor).rowTypes(rowTypes).columnTypes(columnTypes);
		}

		@Override
		public <B extends Block, R extends IBlockSubtype, C extends IBlockSubtype> FeatureBlockTable.Builder<B, R, C> blockTable(BiFunction<R, C, B> constructor, R[] rowTypes, C[] columnTypes) {
			return (FeatureBlockTable.Builder<B, R, C>) new FeatureBlockTable.Builder<>(this, constructor).rowTypes(rowTypes).columnTypes(columnTypes);
		}

		@Override
		public FeatureFluid.Builder fluid(String identifier) {
			return new FeatureFluid.Builder(this, this.moduleId, identifier);
		}

		@Override
		public <R extends Recipe<?>> FeatureRecipeType<R> recipeType(String name, Supplier<RecipeSerializer<? extends R>> serializer) {
			return new FeatureRecipeType<>(this, this.moduleId, name, serializer);
		}

		@Override
		public void addRegistryListener(ResourceKey<? extends Registry<?>> type, Runnable listener) {
			ModUtil.addRegistryListener(type, listener);
		}

		public <F extends IModFeature> F register(F feature) {
			this.features.add(feature);
			this.featureByRegistry.put(feature.getRegistry(), feature);
			return feature;
		}

		@Override
		public <T extends BlockEntity> FeatureTileType<T> tile(BlockEntityType.BlockEntitySupplier<T> constructor, String identifier, Supplier<Collection<? extends Block>> validBlocks) {
			return register(new FeatureTileType<>(this, this.moduleId, identifier, constructor, validBlocks));
		}

		@Override
		public <C extends AbstractContainerMenu> FeatureMenuType<C> menuType(IContainerFactory<C> factory, String identifier) {
			return register(new FeatureMenuType<>(this, this.moduleId, identifier, factory));
		}

		@Override
		public <E extends Entity> FeatureEntityType<E> entity(EntityType.EntityFactory<E> factory, MobCategory classification, String identifier) {
			return entity(factory, classification, identifier, (builder) -> builder);
		}

		@Override
		public <E extends Entity> FeatureEntityType<E> entity(EntityType.EntityFactory<E> factory, MobCategory classification, String identifier, UnaryOperator<EntityType.Builder<E>> consumer) {
			return entity(factory, classification, identifier, consumer, LivingEntity::createLivingAttributes);
		}

		@Override
		public <E extends Entity> FeatureEntityType<E> entity(EntityType.EntityFactory<E> factory, MobCategory classification, String identifier, UnaryOperator<EntityType.Builder<E>> consumer, Supplier<AttributeSupplier.Builder> attributes) {
			return register(new FeatureEntityType<>(this, this.moduleId, identifier, consumer, factory, classification, attributes));
		}

		@Override
		public FeatureCreativeTab creativeTab(String id, Consumer<CreativeModeTab.Builder> builder) {
			return register(new FeatureCreativeTab(this, this.moduleId, id, builder));
		}

		@Override
		public Collection<IModFeature> getFeatures() {
			return this.features;
		}

		@Override
		public Collection<IModFeature> getFeatures(ResourceKey<? extends Registry<?>> type) {
			return this.featureByRegistry.get(type);
		}

		@Override
		public ResourceLocation getModuleId() {
			return this.moduleId;
		}
	}
}
