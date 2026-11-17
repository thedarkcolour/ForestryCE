package forestry.core.content.sorting.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.sorting.tiles.GeneticFilterBlockEntity;

@FeatureProvider
public class SortingBlockEntities {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.SORTING);

	public static final FeatureTileType<GeneticFilterBlockEntity> GENETIC_FILTER = REGISTRY.tile(GeneticFilterBlockEntity::new, "genetic_filter", SortingBlocks.FILTER::collect);
}
