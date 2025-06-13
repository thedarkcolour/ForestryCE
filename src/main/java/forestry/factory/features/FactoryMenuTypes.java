package forestry.factory.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.factory.gui.*;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class FactoryMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FACTORY);

	public static final FeatureMenuType<BottlerMenu> BOTTLER = REGISTRY.menuType(BottlerMenu::fromNetwork, "bottler");
	public static final FeatureMenuType<CarpenterMenu> CARPENTER = REGISTRY.menuType(CarpenterMenu::fromNetwork, "carpenter");
	public static final FeatureMenuType<CentrifugeMenu> CENTRIFUGE = REGISTRY.menuType(CentrifugeMenu::fromNetwork, "centrifuge");
	public static final FeatureMenuType<FabricatorMenu> FABRICATOR = REGISTRY.menuType(FabricatorMenu::fromNetwork, "fabricator");
	public static final FeatureMenuType<FermenterMenu> FERMENTER = REGISTRY.menuType(FermenterMenu::fromNetwork, "fermenter");
	public static final FeatureMenuType<MoistenerMenu> MOISTENER = REGISTRY.menuType(MoistenerMenu::fromNetwork, "moistener");
	public static final FeatureMenuType<RaintankMenu> RAINTANK = REGISTRY.menuType(RaintankMenu::fromNetwork, "raintank");
	public static final FeatureMenuType<SqueezerMenu> SQUEEZER = REGISTRY.menuType(SqueezerMenu::fromNetwork, "squeezer");
	public static final FeatureMenuType<StillMenu> STILL = REGISTRY.menuType(StillMenu::fromNetwork, "still");
}
