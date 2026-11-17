package forestry.core.content.sorting.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureMenuType;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.sorting.gui.GeneticFilterMenu;

@FeatureProvider
public class SortingMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.SORTING);

	public static final FeatureMenuType<GeneticFilterMenu> GENETIC_FILTER = REGISTRY.menuType(GeneticFilterMenu::fromNetwork, "genetic_filter");
}
