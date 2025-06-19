package forestry.core.network.packets;

import forestry.api.core.IErrorLogicSource;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketErrorUpdate(BlockPos pos, short[] errorStates) implements IForestryPacketClient {
	public PacketErrorUpdate(BlockEntity tile, IErrorLogicSource errorLogicSource) {
		this(tile.getBlockPos(), errorLogicSource.getErrorLogic().toArray());
	}

	public Type<?> type() {
		return PacketIdClient.ERROR_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketErrorUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeShortArray(buffer, msg.errorStates);
	}

	public static PacketErrorUpdate decode(RegistryFriendlyByteBuf buffer) {
		BlockPos pos = buffer.readBlockPos();
		short[] errorStats = NetworkUtil.readShortArray(buffer);
		return new PacketErrorUpdate(pos, errorStats);
	}

	public static void handle(PacketErrorUpdate msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, IErrorLogicSource.class, errorSourceTile -> errorSourceTile.getErrorLogic().fromArray(msg.errorStates));
	}
}
