package forestry.mail.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.config.ForestryConfig;
import forestry.core.network.PacketIdClient;
import forestry.mail.carriers.players.POBoxInfo;
import forestry.mail.gui.ToastMailboxInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPOBoxInfoResponse(int playerLetters, int tradeLetters,
									  boolean silent) implements IForestryPacketClient {
	public PacketPOBoxInfoResponse(POBoxInfo info, boolean silent) {
		this(info.playerLetters(), info.tradeLetters(), silent);
	}

	public Type<?> type() {
		return PacketIdClient.POBOX_INFO_RESPONSE;
	}

	public static void write(RegistryFriendlyByteBuf buffer, PacketPOBoxInfoResponse msg) {
		buffer.writeInt(msg.playerLetters);
		buffer.writeInt(msg.tradeLetters);
		buffer.writeBoolean(msg.silent);
	}

	public static PacketPOBoxInfoResponse decode(RegistryFriendlyByteBuf buffer) {
		return new PacketPOBoxInfoResponse(buffer.readInt(), buffer.readInt(), buffer.readBoolean());
	}

	public static void handle(PacketPOBoxInfoResponse msg, IPayloadContext ctx) {
		POBoxInfo poBox = new POBoxInfo(msg.playerLetters, msg.tradeLetters);
		if (player.equals(Minecraft.getInstance().player) && ForestryConfig.CLIENT.mailAlertsEnabled.get()) {
			ToastMailboxInfo.addOrUpdate(Minecraft.getInstance().getToasts(), poBox, msg.silent);
		}
	}
}
