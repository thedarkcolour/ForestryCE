package forestry.mail.client;

import net.minecraft.client.gui.screens.MenuScreens;

import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import forestry.api.client.IClientModuleHandler;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.gui.GuiCatalogue;
import forestry.mail.gui.GuiLetter;
import forestry.mail.gui.GuiMailbox;
import forestry.mail.gui.GuiStampCollector;
import forestry.mail.gui.TradeStationNamingScreen;
import forestry.mail.gui.TradeStationScreen;

public class MailClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(MailClientHandler::onClientSetup);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(MailMenuTypes.CATALOGUE.menuType(), GuiCatalogue::new);
			MenuScreens.register(MailMenuTypes.LETTER.menuType(), GuiLetter::new);
			MenuScreens.register(MailMenuTypes.MAILBOX.menuType(), GuiMailbox::new);
			MenuScreens.register(MailMenuTypes.STAMP_COLLECTOR.menuType(), GuiStampCollector::new);
			MenuScreens.register(MailMenuTypes.TRADE_NAME.menuType(), TradeStationNamingScreen::new);
			MenuScreens.register(MailMenuTypes.TRADER.menuType(), TradeStationScreen::new);
		});
	}
}
