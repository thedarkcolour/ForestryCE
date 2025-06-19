package forestry.mail.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.mail.tiles.TileTrader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTraderAddressRequest(BlockPos pos, String addressName) implements IForestryPacketServer {
	public PacketTraderAddressRequest(TileTrader tile, String addressName) {
		this(tile.getBlockPos(), addressName);
	}

	public static void handle(PacketTraderAddressRequest msg, IPayloadContext context) {
		TileUtil.actOnTile(context.player().level(), msg.pos(), TileTrader.class, tile -> {
			if (tile.handleSetAddressRequest(msg.addressName())) {
				context.player().openMenu(tile, msg.pos());
			}
		});
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.TRADING_ADDRESS_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketTraderAddressRequest msg) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeUtf(msg.addressName);
	}

	public static PacketTraderAddressRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketTraderAddressRequest(buffer.readBlockPos(), buffer.readUtf());
	}
}
