package forestry.mail.network.packets;

import com.mojang.authlib.GameProfile;
import forestry.api.mail.IMailAddress;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.mail.MailAddress;
import forestry.mail.features.PostalCarriers;
import forestry.mail.gui.ILetterInfoReceiver;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketLetterInfoResponsePlayer(IMailAddress address) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.LETTER_INFO_RESPONSE_PLAYER;
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		GameProfile profile = this.address.getPlayerProfile();
		buffer.writeUUID(profile.getId());
		buffer.writeUtf(profile.getName());
	}

	public static PacketLetterInfoResponsePlayer decode(RegistryFriendlyByteBuf buffer) {
		return new PacketLetterInfoResponsePlayer(new MailAddress(new GameProfile(buffer.readUUID(), buffer.readUtf())));
	}

	public static void handle(PacketLetterInfoResponsePlayer msg, IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof ILetterInfoReceiver receiver) {
			receiver.handleLetterInfoUpdate(PostalCarriers.PLAYER.get(), msg.address, null);
		}
	}
}
