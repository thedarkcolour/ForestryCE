package forestry.arboriculture.network;

import forestry.api.modules.IForestryPacketClient;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRipeningUpdate(BlockPos pos, int value) implements IForestryPacketClient {
	public PacketRipeningUpdate(TileLeaves leaves) {
		this(leaves.getBlockPos(), leaves.getFruitColour());
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.RIPENING_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketRipeningUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeVarInt(msg.value);
	}

	public static PacketRipeningUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRipeningUpdate(buffer.readBlockPos(), buffer.readVarInt());
	}

	public static void handle(PacketRipeningUpdate msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, TileLeaves.class, tile -> tile.fromRipeningPacket(msg.value));
	}
}
