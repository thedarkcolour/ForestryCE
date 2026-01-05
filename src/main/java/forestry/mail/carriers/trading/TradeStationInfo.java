package forestry.mail.carriers.trading;

import com.mojang.authlib.GameProfile;
import forestry.api.mail.EnumTradeStationState;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.ITradeStationInfo;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record TradeStationInfo(IMailAddress address, GameProfile owner, ItemStack tradegood, List<ItemStack> required,
							   EnumTradeStationState state) implements ITradeStationInfo {
	public TradeStationInfo {
		if (!address.getCarrier().equals(PostalCarriers.TRADER.get())) {
			throw new IllegalArgumentException("TradeStation address must be a trader");
		}
	}
}
