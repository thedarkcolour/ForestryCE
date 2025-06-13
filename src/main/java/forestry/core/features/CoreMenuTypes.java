package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.circuits.SolderingIronMenu;
import forestry.core.gui.PortableAnalyzerMenu;
import forestry.core.gui.AnalyzerMenu;
import forestry.core.gui.EscritoireMenu;
import forestry.core.gui.NaturalistInventoryMenu;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CoreMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureMenuType<PortableAnalyzerMenu> ALYZER = REGISTRY.menuType(PortableAnalyzerMenu::fromNetwork, "alyzer");
	public static final FeatureMenuType<AnalyzerMenu> ANALYZER = REGISTRY.menuType(AnalyzerMenu::fromNetwork, "analyzer");
	public static final FeatureMenuType<EscritoireMenu> ESCRITOIRE = REGISTRY.menuType(EscritoireMenu::fromNetwork, "escritoire");
	public static final FeatureMenuType<NaturalistInventoryMenu> NATURALIST_INVENTORY = REGISTRY.menuType(NaturalistInventoryMenu::fromNetwork, "naturalist_inventory");
	public static final FeatureMenuType<SolderingIronMenu> SOLDERING_IRON = REGISTRY.menuType(SolderingIronMenu::fromNetwork, "soldering_iron");
}
