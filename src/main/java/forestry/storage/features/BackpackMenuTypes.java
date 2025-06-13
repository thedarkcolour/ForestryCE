package forestry.storage.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.storage.gui.BackpackMenu;
import forestry.storage.gui.NaturalistBackpackMenu;

@FeatureProvider
public class BackpackMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.STORAGE);

	public static final FeatureMenuType<BackpackMenu> BACKPACK = REGISTRY.menuType(BackpackMenu::fromNetwork, "backpack");
	public static final FeatureMenuType<NaturalistBackpackMenu> NATURALIST_BACKPACK = REGISTRY.menuType(NaturalistBackpackMenu::fromNetwork, "naturalist_backpack");
}
