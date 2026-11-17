package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.engine.circuits.SolderingIronMenu;
import forestry.core.content.analyzer.PortableAnalyzerMenu;
import forestry.core.content.analyzer.AnalyzerMenu;
import forestry.core.content.burnbarrel.BurnBarrelMenu;
import forestry.core.content.escritoire.EscritoireMenu;
import forestry.core.platform.gui.NaturalistInventoryMenu;
import forestry.core.platform.registration.FeatureMenuType;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class CoreMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureMenuType<PortableAnalyzerMenu> PORTABLE_ANALYZER = REGISTRY.menuType(PortableAnalyzerMenu::fromNetwork, "portable_analyzer");
	public static final FeatureMenuType<AnalyzerMenu> ANALYZER = REGISTRY.menuType(AnalyzerMenu::fromNetwork, "analyzer");
	public static final FeatureMenuType<EscritoireMenu> ESCRITOIRE = REGISTRY.menuType(EscritoireMenu::fromNetwork, "escritoire");
	public static final FeatureMenuType<BurnBarrelMenu> BURN_BARREL = REGISTRY.menuType(BurnBarrelMenu::fromNetwork, "burn_barrel");
	public static final FeatureMenuType<NaturalistInventoryMenu> NATURALIST_INVENTORY = REGISTRY.menuType(NaturalistInventoryMenu::fromNetwork, "naturalist_inventory");
	public static final FeatureMenuType<SolderingIronMenu> SOLDERING_IRON = REGISTRY.menuType(SolderingIronMenu::fromNetwork, "soldering_iron");
}
