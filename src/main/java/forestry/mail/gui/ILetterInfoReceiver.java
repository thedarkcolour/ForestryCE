package forestry.mail.gui;

import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostalCarrier;
import forestry.api.mail.ITradeStationInfo;

import javax.annotation.Nullable;

public interface ILetterInfoReceiver {
	void handleLetterInfoUpdate(IPostalCarrier carrier, @Nullable IMailAddress address, @Nullable ITradeStationInfo tradeInfo);
}
