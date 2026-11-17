package forestry.core.content.backpacks.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureMenuType;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.backpacks.gui.BackpackMenu;
import forestry.core.content.backpacks.gui.NaturalistBackpackMenu;

@FeatureProvider
public class BackpackMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.STORAGE);

	public static final FeatureMenuType<BackpackMenu> BACKPACK = REGISTRY.menuType(BackpackMenu::fromNetwork, "backpack");
	public static final FeatureMenuType<NaturalistBackpackMenu> NATURALIST_BACKPACK = REGISTRY.menuType(NaturalistBackpackMenu::fromNetwork, "naturalist_backpack");
}
