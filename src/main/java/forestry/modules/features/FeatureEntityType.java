package forestry.modules.features;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import net.minecraftforge.registries.RegistryObject;

public class FeatureEntityType<T extends Entity> extends ModFeature implements IEntityTypeFeature<T> {
	protected final Supplier<AttributeSupplier.Builder> attributes;
	protected final EntityType.EntityFactory<T> factory;
	protected final MobCategory classification;
	private final RegistryObject<EntityType<T>> entityTypeObject;

	public FeatureEntityType(IFeatureRegistry registry, ResourceLocation moduleId, String name, UnaryOperator<EntityType.Builder<T>> consumer, EntityType.EntityFactory<T> factory, MobCategory classification, Supplier<AttributeSupplier.Builder> attributes) {
		super(moduleId, name);
		this.factory = factory;
		this.attributes = attributes;
		this.classification = classification;
		this.entityTypeObject = registry.getRegistry(Registries.ENTITY_TYPE).register(name, () -> consumer.apply(EntityType.Builder.of(factory, classification)).build(getModuleId().getNamespace() + ":" + name));
	}

	@Override
	public AttributeSupplier.Builder createAttributes() {
		return attributes.get();
	}

	@Override
	public EntityType<T> entityType() {
		return entityTypeObject.get();
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.ENTITY_TYPE;
	}
}
