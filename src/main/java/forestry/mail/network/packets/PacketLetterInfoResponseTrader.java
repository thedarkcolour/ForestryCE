package forestry.mail.network.packets;

import com.mojang.authlib.GameProfile;
import forestry.api.mail.EnumTradeStationState;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.TradeStationInfo;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.NetworkUtil;
import forestry.mail.MailAddress;
import forestry.mail.features.PostalCarriers;
import forestry.mail.gui.ILetterInfoReceiver;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.List;

public record PacketLetterInfoResponseTrader(@Nullable TradeStationInfo info) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.LETTER_INFO_RESPONSE_TRADER;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketLetterInfoResponseTrader msg) {
		if (msg.info == null) {
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			buffer.writeUtf(msg.info.address().getName());

			GameProfile profile = msg.info.owner();
			buffer.writeUUID(profile.getId());
			buffer.writeUtf(profile.getName());

			ItemStack.STREAM_CODEC.encode(buffer, msg.info.tradegood());
			NetworkUtil.writeItemStacks(buffer, msg.info.required());

			buffer.writeEnum(msg.info.state());
		}
	}

	public static PacketLetterInfoResponseTrader decode(RegistryFriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			IMailAddress address = new MailAddress(buffer.readUtf());
			GameProfile owner = new GameProfile(buffer.readUUID(), buffer.readUtf());
			ItemStack tradegood = ItemStack.STREAM_CODEC.decode(buffer);
			List<ItemStack> required = NetworkUtil.readItemStacks(buffer);
			EnumTradeStationState state = buffer.readEnum(EnumTradeStationState.class);
			return new PacketLetterInfoResponseTrader(new TradeStationInfo(address, owner, tradegood, required, state));
		} else {
			return new PacketLetterInfoResponseTrader(null);
		}
	}

	public static void handle(PacketLetterInfoResponseTrader msg, IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof ILetterInfoReceiver receiver) {
			receiver.handleLetterInfoUpdate(PostalCarriers.TRADER.value(), null, msg.info);
		}
	}
}
