package forestry.mail.network.packets;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.mojang.authlib.GameProfile;

import forestry.api.mail.EnumAddressee;
import forestry.api.mail.EnumTradeStationState;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.ITradeStationInfo;
import forestry.api.mail.PostManager;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.NetworkUtil;
import forestry.mail.TradeStationInfo;
import forestry.mail.gui.ILetterInfoReceiver;

public record PacketLetterInfoResponseTrader(@Nullable ITradeStationInfo info) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.LETTER_INFO_RESPONSE_TRADER;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		if (info == null) {
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			buffer.writeUtf(info.address().getName());

			GameProfile profile = info.owner();
			buffer.writeUUID(profile.getId());
			buffer.writeUtf(profile.getName());

			buffer.writeItem(info.tradegood());
			NetworkUtil.writeItemStacks(buffer, info.required());

			buffer.writeEnum(info.state());
		}
	}

	public static PacketLetterInfoResponseTrader decode(FriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			IMailAddress address = PostManager.postRegistry.createMailAddress(buffer.readUtf());
			GameProfile owner = new GameProfile(buffer.readUUID(), buffer.readUtf());
			ItemStack tradegood = buffer.readItem();
			NonNullList<ItemStack> required = NetworkUtil.readItemStacks(buffer);
			EnumTradeStationState state = buffer.readEnum(EnumTradeStationState.class);
			return new PacketLetterInfoResponseTrader(new TradeStationInfo(address, owner, tradegood, required, state));
		} else {
			return new PacketLetterInfoResponseTrader(null);
		}
	}

	public static void handle(PacketLetterInfoResponseTrader msg, Player player) {
		if (player.containerMenu instanceof ILetterInfoReceiver receiver) {
			receiver.handleLetterInfoUpdate(EnumAddressee.TRADER, null, msg.info);
		}
	}
}
