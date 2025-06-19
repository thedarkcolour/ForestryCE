package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.IStreamable;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public class PacketTileStream implements IForestryPacketClient {
	protected final BlockPos pos;
	@Nullable
	protected final IStreamable streamable;
	@Nullable
	protected final RegistryFriendlyByteBuf payload;

	public <T extends BlockEntity & IStreamable> PacketTileStream(T streamable) {
		this.pos = streamable.getBlockPos();
		this.streamable = streamable;
		this.payload = null;
	}

	private PacketTileStream(BlockPos pos, RegistryFriendlyByteBuf payload) {
		this.pos = pos;
		this.streamable = null;
		this.payload = payload;
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.TILE_STREAM;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketTileStream msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writePayloadBuffer(buffer, msg.streamable::writeData);
	}

	public static PacketTileStream decode(RegistryFriendlyByteBuf buffer) {
		return new PacketTileStream(buffer.readBlockPos(), NetworkUtil.readPayloadBuffer(buffer));
	}

	public static void handle(PacketTileStream msg, IPayloadContext ctx) {
		IStreamable tile = TileUtil.getTile(ctx.player().level(), msg.pos, IStreamable.class);

		if (tile != null) {
			tile.readData(msg.payload);
		}
	}
}
