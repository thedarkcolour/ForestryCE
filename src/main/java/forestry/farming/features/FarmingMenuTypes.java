package forestry.farming.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.farming.gui.FarmMenu;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class FarmingMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureMenuType<FarmMenu> FARM = REGISTRY.menuType(FarmMenu::fromNetwork, "farm");
}
