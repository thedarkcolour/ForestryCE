package forestry.mail.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.mail.gui.ContainerCatalogue;
import forestry.mail.gui.ContainerLetter;
import forestry.mail.gui.ContainerMailbox;
import forestry.mail.gui.ContainerStampCollector;
import forestry.mail.gui.TradeStationNamingMenu;
import forestry.mail.gui.TradeStationMenu;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class MailMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.MAIL);

	public static final FeatureMenuType<ContainerCatalogue> CATALOGUE = REGISTRY.menuType(ContainerCatalogue::fromNetwork, "catalogue");
	public static final FeatureMenuType<ContainerLetter> LETTER = REGISTRY.menuType(ContainerLetter::fromNetwork, "letter");
	public static final FeatureMenuType<ContainerMailbox> MAILBOX = REGISTRY.menuType(ContainerMailbox::fromNetwork, "mailbox");
	public static final FeatureMenuType<ContainerStampCollector> STAMP_COLLECTOR = REGISTRY.menuType(ContainerStampCollector::fromNetwork, "stamp_collector");
	public static final FeatureMenuType<TradeStationNamingMenu> TRADE_NAME = REGISTRY.menuType(TradeStationNamingMenu::fromNetwork, "trade_name");
	public static final FeatureMenuType<TradeStationMenu> TRADER = REGISTRY.menuType(TradeStationMenu::fromNetwork, "trader");
}
