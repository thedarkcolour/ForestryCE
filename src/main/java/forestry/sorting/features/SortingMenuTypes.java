package forestry.sorting.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.sorting.gui.GeneticFilterMenu;

@FeatureProvider
public class SortingMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.SORTING);

	public static final FeatureMenuType<GeneticFilterMenu> GENETIC_FILTER = REGISTRY.menuType(GeneticFilterMenu::fromNetwork, "genetic_filter");
}
