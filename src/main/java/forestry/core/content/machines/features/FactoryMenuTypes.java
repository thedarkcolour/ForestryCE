package forestry.core.content.machines.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.content.machines.gui.*;
import forestry.core.platform.registration.FeatureMenuType;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class FactoryMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FACTORY);

	public static final FeatureMenuType<BottlerMenu> BOTTLER = REGISTRY.menuType(BottlerMenu::fromNetwork, "bottler");
	public static final FeatureMenuType<CarpenterMenu> CARPENTER = REGISTRY.menuType(CarpenterMenu::fromNetwork, "carpenter");
	public static final FeatureMenuType<CentrifugeMenu> CENTRIFUGE = REGISTRY.menuType(CentrifugeMenu::fromNetwork, "centrifuge");
	public static final FeatureMenuType<FabricatorMenu> FABRICATOR = REGISTRY.menuType(FabricatorMenu::fromNetwork, "thermionic_fabricator");
	public static final FeatureMenuType<FermenterMenu> FERMENTER = REGISTRY.menuType(FermenterMenu::fromNetwork, "fermenter");
	public static final FeatureMenuType<MoistenerMenu> MOISTENER = REGISTRY.menuType(MoistenerMenu::fromNetwork, "moistener");
	public static final FeatureMenuType<SmelterMenu> SMELTER = REGISTRY.menuType(SmelterMenu::fromNetwork, "smelter");
	public static final FeatureMenuType<SqueezerMenu> SQUEEZER = REGISTRY.menuType(SqueezerMenu::fromNetwork, "squeezer");
	public static final FeatureMenuType<StillMenu> STILL = REGISTRY.menuType(StillMenu::fromNetwork, "still");
}
