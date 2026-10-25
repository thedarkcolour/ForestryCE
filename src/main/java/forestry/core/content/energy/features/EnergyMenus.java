package forestry.core.content.energy.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.content.energy.menu.BiogasEngineMenu;
import forestry.core.content.energy.menu.CombustionEngineMenu;
import forestry.core.content.energy.menu.PeatEngineMenu;
import forestry.core.content.energy.menu.SolarEngineMenu;
import forestry.core.platform.registration.FeatureMenuType;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class EnergyMenus {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ENERGY);

	public static final FeatureMenuType<BiogasEngineMenu> BIOGAS_ENGINE = REGISTRY.menuType(BiogasEngineMenu::fromNetwork, "biogas_engine");
	public static final FeatureMenuType<CombustionEngineMenu> COMBUSTION_ENGINE = REGISTRY.menuType(CombustionEngineMenu::fromNetwork, "combustion_engine");
	public static final FeatureMenuType<PeatEngineMenu> PEAT_ENGINE = REGISTRY.menuType(PeatEngineMenu::fromNetwork, "peat_engine");
	public static final FeatureMenuType<SolarEngineMenu> SOLAR_ENGINE = REGISTRY.menuType(SolarEngineMenu::fromNetwork, "solar_engine");
}
