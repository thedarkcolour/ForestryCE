package forestry.mail.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.mail.gui.ContainerLetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record PacketLetterTextSet(String string) implements IForestryPacketServer {
	public static void handle(PacketLetterTextSet msg, ServerPlayer player) {
		if (player.containerMenu instanceof ContainerLetter letterMenu) {
			letterMenu.handleSetText(msg.string());
		}
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.LETTER_TEXT_SET;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.string);
	}

	public static PacketLetterTextSet decode(FriendlyByteBuf buffer) {
		return new PacketLetterTextSet(buffer.readUtf());
	}
}
