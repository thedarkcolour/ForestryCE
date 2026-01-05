package forestry.mail.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.config.ForestryConfig;
import forestry.core.network.PacketIdClient;
import forestry.mail.carriers.players.POBoxInfo;
import forestry.mail.gui.ToastMailboxInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketPOBoxInfoResponse(int playerLetters, int tradeLetters,
									  boolean silent) implements IForestryPacketClient {
	public PacketPOBoxInfoResponse(POBoxInfo info, boolean silent) {
		this(info.playerLetters(), info.tradeLetters(), silent);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.POBOX_INFO_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(this.playerLetters);
		buffer.writeInt(this.tradeLetters);
		buffer.writeBoolean(this.silent);
	}

	public static PacketPOBoxInfoResponse decode(FriendlyByteBuf buffer) {
		return new PacketPOBoxInfoResponse(buffer.readInt(), buffer.readInt(), buffer.readBoolean());
	}

	public static void handle(PacketPOBoxInfoResponse msg, Player player) {
		POBoxInfo poBox = new POBoxInfo(msg.playerLetters, msg.tradeLetters);
		if (player.equals(Minecraft.getInstance().player) && ForestryConfig.CLIENT.mailAlertsEnabled.get()) {
			ToastMailboxInfo.addOrUpdate(Minecraft.getInstance().getToasts(), poBox, msg.silent);
		}
	}
}
