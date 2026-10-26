package forestry.agriculture.features;

import forestry.agriculture.minifarm.gui.MinifarmMenu;
import forestry.agriculture.multifarm.gui.MultifarmMenu;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureMenuType;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class FarmingMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureMenuType<MultifarmMenu> MULTIFARM = REGISTRY.menuType(MultifarmMenu::fromNetwork, "multifarm");
	public static final FeatureMenuType<MinifarmMenu> MINIFARM = REGISTRY.menuType(MinifarmMenu::fromNetwork, "minifarm");
}
