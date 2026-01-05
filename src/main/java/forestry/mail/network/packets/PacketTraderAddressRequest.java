package forestry.mail.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.mail.tiles.TileTrader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

public record PacketTraderAddressRequest(BlockPos pos, String addressName) implements IForestryPacketServer {
	public PacketTraderAddressRequest(TileTrader tile, String addressName) {
		this(tile.getBlockPos(), addressName);
	}

	public static void handle(PacketTraderAddressRequest msg, ServerPlayer player) {
		TileUtil.actOnTile(player.level(), msg.pos(), TileTrader.class, tile -> {
			if (tile.handleSetAddressRequest(msg.addressName())) {
				NetworkHooks.openScreen(player, tile, msg.pos());
			}
		});
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.TRADING_ADDRESS_REQUEST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeUtf(this.addressName);
	}

	public static PacketTraderAddressRequest decode(FriendlyByteBuf buffer) {
		return new PacketTraderAddressRequest(buffer.readBlockPos(), buffer.readUtf());
	}
}
