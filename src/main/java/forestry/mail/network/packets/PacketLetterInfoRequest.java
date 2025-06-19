package forestry.mail.network.packets;

import forestry.api.ForestryRegistries;
import forestry.api.mail.IPostalCarrier;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.mail.gui.LetterMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketLetterInfoRequest(String recipientName, IPostalCarrier addressType) implements IForestryPacketServer {
	public static void handle(PacketLetterInfoRequest msg, IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof LetterMenu containerLetter) {
			containerLetter.handleRequestLetterInfo(ctx.player(), msg.recipientName(), msg.addressType());
		}
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.LETTER_INFO_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketLetterInfoRequest msg) {
		buffer.writeUtf(msg.recipientName);
		buffer.writeUtf(ForestryRegistries.POSTAL_CARRIER.getKey(msg.addressType).toString());
	}

	public static PacketLetterInfoRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketLetterInfoRequest(buffer.readUtf(), ForestryRegistries.POSTAL_CARRIER.get(ResourceLocation.tryParse(buffer.readUtf())));
	}
}
