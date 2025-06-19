package forestry.api.mail;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import forestry.mail.features.PostalCarriers;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record TradeStationInfo(
	IMailAddress address,
	GameProfile owner,
	ItemStack tradegood,
	List<ItemStack> required,
	EnumTradeStationState state
) {
	public TradeStationInfo {
		Preconditions.checkArgument(address.getCarrier().equals(PostalCarriers.TRADER.value()), "TradeStation address must be a trader");
	}
}
