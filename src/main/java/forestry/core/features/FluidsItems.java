package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.items.ItemFluidContainerForestry;
import forestry.core.items.definitions.EnumContainerType;
import forestry.modules.features.FeatureItemGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class FluidsItems {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureItemGroup<ItemFluidContainerForestry, EnumContainerType> CONTAINERS = REGISTRY.itemGroup(ItemFluidContainerForestry::new, EnumContainerType.values()).create();
}
