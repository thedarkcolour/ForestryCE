package forestry.apiculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.gui.*;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class ApicultureMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final FeatureMenuType<AlvearyMenu> ALVEARY = REGISTRY.menuType(AlvearyMenu::fromNetwork, "alveary");
	public static final FeatureMenuType<AlvearyHygroregulatorMenu> ALVEARY_HYGROREGULATOR = REGISTRY.menuType(AlvearyHygroregulatorMenu::fromNetwork, "alveary_hygroregulator");
	public static final FeatureMenuType<AlvearySieveMenu> ALVEARY_SIEVE = REGISTRY.menuType(AlvearySieveMenu::fromNetwork, "alveary_sieve");
	public static final FeatureMenuType<AlvearySwarmerMenu> ALVEARY_SWARMER = REGISTRY.menuType(AlvearySwarmerMenu::fromNetwork, "alveary_swarmer");
	public static final FeatureMenuType<BeeHousingMenu> BEE_HOUSING = REGISTRY.menuType(BeeHousingMenu::fromNetwork, "bee_housing");
}
