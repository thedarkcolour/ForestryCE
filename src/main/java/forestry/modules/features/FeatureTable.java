package forestry.modules.features;

import com.google.common.collect.ImmutableTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import forestry.api.core.IFeatureSubtype;
import forestry.api.core.IItemProvider;
import forestry.modules.features.FeatureGroup.IdentifierType;

public abstract class FeatureTable<B extends FeatureTable.Builder<R, C, ? extends FeatureTable<B, F, R, C>>, F extends IModFeature, R extends IFeatureSubtype, C extends IFeatureSubtype> {
	protected final ImmutableTable<R, C, F> featureByTypes;

	public FeatureTable(B builder) {
		ImmutableTable.Builder<R, C, F> mapBuilder = new ImmutableTable.Builder<>();
		for (R row : builder.rowTypes) {
			for (C column : builder.columnTypes) {
				mapBuilder.put(row, column, createFeature(builder, row, column));
			}
		}
		featureByTypes = mapBuilder.build();
	}

	protected abstract F createFeature(B builder, R rowType, C columnType);

	public boolean has(R rowType, C columnType) {
		return featureByTypes.contains(rowType, columnType);
	}

	public F get(R rowType, C columnType) {
		return featureByTypes.get(rowType, columnType);
	}

	public ImmutableTable<R, C, F> getFeatureByTypes() {
		return featureByTypes;
	}

	public Collection<F> getRowFeatures(R rowType) {
		return featureByTypes.row(rowType).values();
	}

	public Collection<F> getColumnFeatures(C rowType) {
		return featureByTypes.column(rowType).values();
	}

	public Collection<F> getFeatures() {
		return featureByTypes.values();
	}

	public boolean itemEqual(ItemStack stack) {
		for (F feature : getFeatures()) {
			if (feature instanceof IItemProvider<?> provider && provider.itemEqual(stack)) {
				return true;
			}
		}

		return false;
	}

	public boolean itemEqual(Item item) {
		for (F feature : getFeatures()) {
			if (feature instanceof IItemProvider<?> provider && provider.itemEqual(item)) {
				return true;
			}
		}

		return false;
	}

	public ItemStack stack(R rowType, C columnType) {
		return stack(rowType, columnType, 1);
	}

	public ItemStack stack(R rowType, C columnType, int amount) {
		if (featureByTypes.get(rowType, columnType) instanceof IItemProvider<?> provider) {
			return provider.stack(amount);
		} else {
			throw new IllegalStateException("This feature group has no item registered for the given sub type to create a stack for.");
		}
	}

	public static abstract class Builder<R extends IFeatureSubtype, C extends IFeatureSubtype, G> {
		protected final IFeatureRegistry registry;
		protected final Set<R> rowTypes = new LinkedHashSet<>();
		protected final Set<C> columnTypes = new LinkedHashSet<>();
		protected IdentifierType identifierType = IdentifierType.TYPE_ONLY;
		protected String identifier = StringUtils.EMPTY;

		public Builder(IFeatureRegistry registry) {
			this.registry = registry;
		}

		public Builder<R, C, G> identifier(String identifier) {
			return identifier(identifier, IdentifierType.PREFIX);
		}

		public Builder<R, C, G> identifier(String identifier, IdentifierType type) {
			this.identifier = identifier;
			this.identifierType = type;
			return this;
		}

		public Builder<R, C, G> rowType(R type) {
			rowTypes.add(type);
			return this;
		}

		public Builder<R, C, G> rowTypes(R[] types) {
			return rowTypes(Arrays.asList(types));
		}

		public Builder<R, C, G> rowTypes(Collection<R> types) {
			rowTypes.addAll(types);
			return this;
		}

		public Builder<R, C, G> columnType(C type) {
			columnTypes.add(type);
			return this;
		}

		public Builder<R, C, G> columnTypes(C[] types) {
			return columnTypes(Arrays.asList(types));
		}

		public Builder<R, C, G> columnTypes(Collection<C> types) {
			columnTypes.addAll(types);
			return this;
		}

		protected String getIdentifier(IFeatureSubtype rowType, IFeatureSubtype columnType) {
			return identifierType.apply(identifier, rowType.getSerializedName() + "_" + columnType.getSerializedName());
		}

		public abstract G create();
	}
}
