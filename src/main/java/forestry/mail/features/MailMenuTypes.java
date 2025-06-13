package forestry.mail.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.mail.gui.*;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class MailMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.MAIL);

	public static final FeatureMenuType<ContainerCatalogue> CATALOGUE = REGISTRY.menuType(ContainerCatalogue::fromNetwork, "catalogue");
	public static final FeatureMenuType<LetterMenu> LETTER = REGISTRY.menuType(LetterMenu::fromNetwork, "letter");
	public static final FeatureMenuType<MailboxMenu> MAILBOX = REGISTRY.menuType(MailboxMenu::fromNetwork, "mailbox");
	public static final FeatureMenuType<StampCollectorMenu> STAMP_COLLECTOR = REGISTRY.menuType(StampCollectorMenu::fromNetwork, "stamp_collector");
	public static final FeatureMenuType<TradeNameMenu> TRADE_NAME = REGISTRY.menuType(TradeNameMenu::fromNetwork, "trade_name");
	public static final FeatureMenuType<TraderMenu> TRADER = REGISTRY.menuType(TraderMenu::fromNetwork, "trader");
}
