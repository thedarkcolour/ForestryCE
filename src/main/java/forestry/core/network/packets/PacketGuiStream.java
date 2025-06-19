package forestry.core.network.packets;

import forestry.api.core.ILocationProvider;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.IStreamableGui;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Streamable is used on the server side to serialize the packet data (payload is null)
// Payload is used on the client side to sync the packet data (streamable is null)
public record PacketGuiStream(
	BlockPos pos,
	// null on client side
	IStreamableGui guiStreamable,
	// null on server side
	RegistryFriendlyByteBuf payload
) implements IForestryPacketClient {
	public <T extends IStreamableGui & ILocationProvider> PacketGuiStream(T guiStreamable) {
		this(guiStreamable.getBlockPos(), guiStreamable, null);
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketGuiStream msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writePayloadBuffer(buffer, msg.guiStreamable::writeGuiData);
	}

	public static PacketGuiStream decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGuiStream(buffer.readBlockPos(), null, NetworkUtil.readPayloadBuffer(buffer));
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.GUI_STREAM;
	}

	public static void handle(PacketGuiStream msg, IPayloadContext ctx) {
		IStreamableGui tile = TileUtil.getTile(ctx.player().level(), msg.pos, IStreamableGui.class);
		if (tile != null) {
			tile.readGuiData(msg.payload);
		}
	}
}
