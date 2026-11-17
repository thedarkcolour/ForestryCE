package forestry.core.content.sorting.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.registration.FeatureBlock;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.sorting.blocks.GeneticFilterBlock;

@FeatureProvider
public class SortingBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.SORTING);

	public static final FeatureBlock<GeneticFilterBlock, ForestryBlockItem<?>> FILTER = REGISTRY.block(GeneticFilterBlock::new, ForestryBlockItem::new, "genetic_filter");
}
