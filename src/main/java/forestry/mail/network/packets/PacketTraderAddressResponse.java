package forestry.mail.network.packets;

import forestry.api.mail.IMailAddress;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.mail.MailAddress;
import forestry.mail.tiles.TileTrader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketTraderAddressResponse(BlockPos pos, IMailAddress address) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.TRADING_ADDRESS_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeUtf(this.address.getName());
	}

	public static PacketTraderAddressResponse decode(FriendlyByteBuf buffer) {
		return new PacketTraderAddressResponse(buffer.readBlockPos(), new MailAddress(buffer.readUtf()));
	}

	public static void handle(PacketTraderAddressResponse msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, TileTrader.class, tile -> tile.setAddress(msg.address));
	}
}
