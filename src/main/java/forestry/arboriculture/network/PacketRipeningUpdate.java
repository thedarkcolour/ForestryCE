package forestry.arboriculture.network;

import forestry.api.modules.IForestryPacketClient;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketRipeningUpdate(BlockPos pos, int value) implements IForestryPacketClient {
	public PacketRipeningUpdate(TileLeaves leaves) {
		this(leaves.getBlockPos(), leaves.getFruitColour());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.RIPENING_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeVarInt(this.value);
	}

	public static PacketRipeningUpdate decode(FriendlyByteBuf buffer) {
		return new PacketRipeningUpdate(buffer.readBlockPos(), buffer.readVarInt());
	}

	public static void handle(PacketRipeningUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, IRipeningPacketReceiver.class, tile -> tile.fromRipeningPacket(msg.value));
	}
}
