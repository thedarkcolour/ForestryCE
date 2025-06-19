package forestry.mail.network.packets;

import forestry.api.mail.IMailAddress;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.mail.MailAddress;
import forestry.mail.tiles.TileTrader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTraderAddressResponse(BlockPos pos, IMailAddress address) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.TRADING_ADDRESS_RESPONSE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketTraderAddressResponse msg) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeUtf(msg.address.getName());
	}

	public static PacketTraderAddressResponse decode(RegistryFriendlyByteBuf buffer) {
		return new PacketTraderAddressResponse(buffer.readBlockPos(), new MailAddress(buffer.readUtf()));
	}

	public static void handle(PacketTraderAddressResponse msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, TileTrader.class, tile -> tile.setAddress(msg.address));
	}
}
