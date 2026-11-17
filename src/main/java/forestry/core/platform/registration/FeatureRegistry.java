package forestry.core.platform.registration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import forestry.api.core.IBlockSubtype;
import forestry.api.core.IItemSubtype;
import forestry.api.core.backpacks.EnumBackpackType;
import forestry.api.core.backpacks.IBackpackDefinition;
import forestry.core.platform.item.ItemForestry;
import forestry.core.platform.util.datastructures.TriFunction;
import forestry.core.content.backpacks.ModuleStorage;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class FeatureRegistry implements IFeatureRegistry {
	private final ArrayList<IModFeature> features = new ArrayList<>();
	private final ArrayListMultimap<ResourceKey<? extends Registry<?>>, IModFeature> featureByRegistry = ArrayListMultimap.create();
	@SuppressWarnings("rawtypes")
	private final HashMap<ResourceKey, DeferredRegister> registries = new HashMap<>();
	private final LinkedListMultimap<ResourceKey<? extends Registry<?>>, Consumer<RegisterEvent>> registryListeners = LinkedListMultimap.create();

	private final ResourceLocation moduleId;
	private final IEventBus modBus;

	public FeatureRegistry(ResourceLocation moduleId, IEventBus modBus) {
		this.moduleId = moduleId;
		this.modBus = modBus;
		this.modBus.addListener(this::onRegisterEvent);
	}

	@SuppressWarnings("unchecked")
	public <V> DeferredRegister<V> getRegistry(ResourceKey<? extends Registry<V>> registryKey) {
		String modId = this.moduleId.getNamespace();
		return this.registries.computeIfAbsent(registryKey, key -> {
			DeferredRegister<V> registry = DeferredRegister.create(key, modId);
			registry.register(this.modBus);
			return registry;
		});
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <V> DeferredRegister<V> getRegistryNullable(ResourceKey<? extends Registry<V>> registry) {
		return this.registries.get(registry);
	}

	public <B extends Block, I extends BlockItem> FeatureBlock<B, I> block(Function<BlockBehaviour.Properties, B> constructor, String name) {
		return block(constructor, null, name);
	}

	public <B extends Block, I extends BlockItem> FeatureBlock<B, I> block(Function<BlockBehaviour.Properties, B> constructor, @Nullable BiFunction<B, Item.Properties, I> itemConstructor, String name) {
		return block(constructor, BlockBehaviour.Properties::of, itemConstructor, Item.Properties::new, name);
	}

	public <B extends Block, I extends BlockItem> FeatureBlock<B, I> block(Function<BlockBehaviour.Properties, B> constructor, Supplier<BlockBehaviour.Properties> blockProperties, @Nullable BiFunction<B, Item.Properties, I> itemConstructor, String name) {
		return block(constructor, blockProperties, itemConstructor, Item.Properties::new, name);
	}

	public <B extends Block, I extends BlockItem> FeatureBlock<B, I> block(Function<BlockBehaviour.Properties, B> constructor, Supplier<BlockBehaviour.Properties> blockProperties, @Nullable BiFunction<B, Item.Properties, I> itemConstructor, Supplier<Item.Properties> itemProperties, String name) {
		ResourceLocation id = this.moduleId.withPath(name);
		Supplier<B> blockSupplier = () -> constructor.apply(IFeatureRegistry.setBlockId(id, blockProperties.get()));
		Function<B, I> itemFunction = itemConstructor == null ? null : (block) -> itemConstructor.apply(block, IFeatureRegistry.setItemId(id, itemProperties.get()));
		return register(new FeatureBlock<>(this, this.moduleId, name, blockSupplier, itemFunction));
	}

	public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(Function<S, B> constructor, Collection<S> types) {
		return blockGroup((properties, type) -> constructor.apply(type), types);
	}

	public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(BiFunction<BlockBehaviour.Properties, S, B> constructor, Collection<S> types) {
		return new FeatureBlockGroup.Builder<>(this, types, constructor);
	}

	@SuppressWarnings("unchecked")
	public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(Collection<S> types) {
		return (FeatureBlockGroup.Builder<B, S>) new FeatureBlockGroup.Builder<>(this, types, (properties, type) -> new Block(properties));
	}

	public <B extends Block, S extends IBlockSubtype> FeatureBlockGroup.Builder<B, S> blockGroup(S[] types) {
		return blockGroup(Arrays.asList(types));
	}

	public <I extends Item> FeatureItem<I> item(Supplier<I> constructor, String name) {
		return register(new FeatureItem<>(this, this.moduleId, name, constructor));
	}

	public FeatureItem<Item> item(String name) {
		return item(ItemForestry::new, name);
	}

	public <I extends Item> FeatureItem<I> item(Function<Item.Properties, I> constructor, Supplier<Item.Properties> properties, String identifier) {
		return register(new FeatureItem<>(this, this.moduleId, identifier, () -> constructor.apply(properties.get())));
	}

	public FeatureItem<Item> backpack(IBackpackDefinition definition, EnumBackpackType type, String identifier) {
		return item(() -> ModuleStorage.BACKPACK_INTERFACE.createBackpack(definition, type), identifier);
	}

	public FeatureItem<Item> naturalistBackpack(IBackpackDefinition definition, ResourceLocation speciesTypeId, CreativeModeTab tab, String identifier) {
		return item(() -> ModuleStorage.BACKPACK_INTERFACE.createNaturalistBackpack(definition, speciesTypeId), identifier);
	}

	public <I extends Item, S extends IItemSubtype> FeatureItemGroup<I, S> itemGroup(Function<S, I> constructor, String identifier, S[] subTypes) {
		return itemGroup(constructor, subTypes).identifier(identifier).create();
	}

	public <I extends Item, S extends IItemSubtype> FeatureItemGroup.Builder<I, S> itemGroup(Function<S, I> constructor, S[] subTypes) {
		return (FeatureItemGroup.Builder<I, S>) new FeatureItemGroup.Builder<>(this, constructor).types(subTypes);
	}

	public <I extends Item, S extends IItemSubtype> FeatureItemGroup.Builder<I, S> itemGroup(BiFunction<S, Item.Properties, I> constructor, Function<S, Item.Properties> properties, S[] subTypes) {
		return (FeatureItemGroup.Builder<I, S>) new FeatureItemGroup.Builder<I, S>(this, s -> constructor.apply(s, properties.apply(s))).types(subTypes);
	}

	public <I extends Item, R extends IItemSubtype, C extends IItemSubtype> FeatureItemTable<I, R, C> itemTable(BiFunction<R, C, I> constructor, R[] rowTypes, C[] columnTypes, String identifier) {
		return itemTable(constructor, rowTypes, columnTypes).identifier(identifier).create();
	}

	public <I extends Item, R extends IItemSubtype, C extends IItemSubtype> FeatureItemTable.Builder<I, R, C> itemTable(BiFunction<R, C, I> constructor, R[] rowTypes, C[] columnTypes) {
		return (FeatureItemTable.Builder<I, R, C>) new FeatureItemTable.Builder<>(this, constructor).rowTypes(rowTypes).columnTypes(columnTypes);
	}

	public <B extends Block, R extends IBlockSubtype, C extends IBlockSubtype> FeatureBlockTable.Builder<B, R, C> blockTable(BiFunction<R, C, B> constructor, R[] rowTypes, C[] columnTypes) {
		return blockTable((properties, rowType, columnType) -> constructor.apply(rowType, columnType), rowTypes, columnTypes);
	}

	public <B extends Block, R extends IBlockSubtype, C extends IBlockSubtype> FeatureBlockTable.Builder<B, R, C> blockTable(TriFunction<BlockBehaviour.Properties, R, C, B> constructor, R[] rowTypes, C[] columnTypes) {
		return (FeatureBlockTable.Builder<B, R, C>) new FeatureBlockTable.Builder<>(this, constructor).rowTypes(rowTypes).columnTypes(columnTypes);
	}

	public FeatureFluid.Builder fluid(String identifier) {
		return new FeatureFluid.Builder(this, this.moduleId, identifier);
	}

	public <R extends Recipe<?>> FeatureRecipeType<R> recipeType(String name, Supplier<RecipeSerializer<? extends R>> serializer) {
		// Every other factory here registers what it builds. This one did not, so recipe types were
		// absent from getFeatures and from anything derived from it
		return register(new FeatureRecipeType<>(this, this.moduleId, name, serializer));
	}

	public void addRegistryListener(ResourceKey<? extends Registry<?>> type, Runnable listener) {
		this.registryListeners.put(type, event -> listener.run());
	}

	private void onRegisterEvent(RegisterEvent event) {
		for (Consumer<RegisterEvent> listener : this.registryListeners.get(event.getRegistryKey())) {
			listener.accept(event);
		}
	}

	public <F extends IModFeature> F register(F feature) {
		this.features.add(feature);
		this.featureByRegistry.put(feature.getRegistry(), feature);
		return feature;
	}

	public <T extends BlockEntity> FeatureTileType<T> tile(BlockEntityType.BlockEntitySupplier<T> constructor, String identifier, Supplier<Collection<? extends Block>> validBlocks) {
		return register(new FeatureTileType<>(this, this.moduleId, identifier, constructor, validBlocks));
	}

	public <C extends AbstractContainerMenu> FeatureMenuType<C> menuType(IContainerFactory<C> factory, String identifier) {
		return register(new FeatureMenuType<>(this, this.moduleId, identifier, factory));
	}

	public <E extends Entity> FeatureEntityType<E> entity(EntityType.EntityFactory<E> factory, MobCategory classification, String identifier) {
		return entity(factory, classification, identifier, (builder) -> builder);
	}

	public <E extends Entity> FeatureEntityType<E> entity(EntityType.EntityFactory<E> factory, MobCategory classification, String identifier, UnaryOperator<EntityType.Builder<E>> consumer) {
		return entity(factory, classification, identifier, consumer, LivingEntity::createLivingAttributes);
	}

	public <E extends Entity> FeatureEntityType<E> entity(EntityType.EntityFactory<E> factory, MobCategory classification, String identifier, UnaryOperator<EntityType.Builder<E>> consumer, Supplier<AttributeSupplier.Builder> attributes) {
		return register(new FeatureEntityType<>(this, this.moduleId, identifier, consumer, factory, classification, attributes));
	}

	public FeatureCreativeTab creativeTab(String id, Consumer<CreativeModeTab.Builder> builder) {
		return register(new FeatureCreativeTab(this, this.moduleId, id, builder));
	}

	public Collection<IModFeature> getFeatures() {
		return this.features;
	}

	/**
	 * Used to get every deferred register this module owns, keyed by registry. Features are only part
	 * of what a module registers - point of interest types go through {@link #getRegistry} directly -
	 * so this is the complete list of what the module puts into the game.
	 *
	 * @return The deferred registers, keyed by registry
	 */
	public Map<ResourceKey, DeferredRegister> getRegistries() {
		return this.registries;
	}

	public Collection<IModFeature> getFeatures(ResourceKey<? extends Registry<?>> type) {
		return this.featureByRegistry.get(type);
	}

	public ResourceLocation getModuleId() {
		return this.moduleId;
	}
}
