package forestry.mail.network.packets;

import forestry.api.mail.IPostalCarrier;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.mail.carriers.PostalCarriers;
import forestry.mail.gui.ContainerLetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record PacketLetterInfoRequest(String recipientName,
									  IPostalCarrier addressType) implements IForestryPacketServer {
	public static void handle(PacketLetterInfoRequest msg, ServerPlayer player) {
		if (player.containerMenu instanceof ContainerLetter containerLetter) {
			containerLetter.handleRequestLetterInfo(player, msg.recipientName(), msg.addressType());
		}
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.LETTER_INFO_REQUEST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.recipientName);
		buffer.writeUtf(PostalCarriers.REGISTRY.get().getKey(this.addressType).toString());
	}

	public static PacketLetterInfoRequest decode(FriendlyByteBuf buffer) {
		return new PacketLetterInfoRequest(buffer.readUtf(), PostalCarriers.REGISTRY.get().getValue(ResourceLocation.tryParse(buffer.readUtf())));
	}
}
