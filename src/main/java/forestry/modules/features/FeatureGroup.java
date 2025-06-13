package forestry.modules.features;

import com.google.common.collect.ImmutableMap;
import forestry.api.core.IFeatureSubtype;
import forestry.api.core.IItemProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class FeatureGroup<B extends FeatureGroup.Builder<S, ? extends FeatureGroup<B, F, S>>, F extends IModFeature, S extends IFeatureSubtype> {
	protected final ImmutableMap<S, F> featureByType;

	protected FeatureGroup(B builder) {
		ImmutableMap.Builder<S, F> mapBuilder = new ImmutableMap.Builder<>();
		builder.subTypes.forEach(subType -> mapBuilder.put(subType, createFeature(builder, subType)));
		this.featureByType = mapBuilder.build();
	}

	protected abstract F createFeature(B builder, S type);

	public boolean has(S subType) {
		return this.featureByType.containsKey(subType);
	}

	public F get(S subType) {
		return this.featureByType.get(subType);
	}

	public ImmutableMap<S, F> getFeatureByType() {
		return this.featureByType;
	}

	public Collection<F> getFeatures() {
		return this.featureByType.values();
	}

	public boolean itemEqual(ItemStack stack) {
		for (F feature : this.getFeatures()) {
			if (feature instanceof FeatureItem<?> itemFeature && itemFeature.itemEqual(stack)) {
				return true;
			}
		}

		return false;
	}

	public boolean itemEqual(Item item) {
		for (F feature : this.getFeatures()) {
			if (feature instanceof FeatureItem<?> itemFeature && itemFeature.itemEqual(item)) {
				return true;
			}
		}

		return false;
	}

	public ItemStack stack(S subType) {
		return stack(subType, 1);
	}

	public ItemStack stack(S subType, int amount) {
		F featureBlock = this.featureByType.get(subType);
		if (featureBlock instanceof IItemProvider<?> item) {
			return item.stack(amount);
		}
		throw new IllegalStateException("This feature group has no item registered for the given sub type to create a stack for.");
	}

	public static abstract class Builder<S extends IFeatureSubtype, G> {
		protected final IFeatureRegistry registry;
		protected final Set<S> subTypes = new LinkedHashSet<>();
		protected IdentifierType identifierType = IdentifierType.TYPE_ONLY;
		protected String identifier = "";

		public Builder(IFeatureRegistry registry) {
			this.registry = registry;
		}

		public Builder<S, G> identifier(String identifier) {
			return identifier(identifier, IdentifierType.PREFIX);
		}

		public Builder<S, G> identifier(String identifier, IdentifierType type) {
			this.identifier = identifier;
			this.identifierType = type;
			return this;
		}

		public Builder<S, G> type(S type) {
			this.subTypes.add(type);
			return this;
		}

		public Builder<S, G> types(S[] types) {
			return types(Arrays.asList(types));
		}

		public Builder<S, G> types(Collection<S> types) {
			this.subTypes.addAll(types);
			return this;
		}

		protected String getIdentifier(IFeatureSubtype type) {
			return this.identifierType.apply(this.identifier, type.identifier());
		}

		public abstract G create();
	}

	public enum IdentifierType implements BiFunction<String, String, String> {
		TYPE_ONLY {
			@Override
			public String apply(String feature, String type) {
				return type;
			}
		},
		PREFIX {
			@Override
			public String apply(String feature, String type) {
				return feature + '_' + type;
			}
		},
		SUFFIX {
			@Override
			public String apply(String feature, String type) {
				return type + '_' + feature;
			}
		}
	}
}
