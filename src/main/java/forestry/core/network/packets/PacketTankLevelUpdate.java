package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.fluids.ITankManager;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTankLevelUpdate(BlockPos pos, int tankIndex, FluidStack contents) implements IForestryPacketClient {
	@Override
	public Type<?> type() {
		return PacketIdClient.TANK_LEVEL_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketTankLevelUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeVarInt(msg.tankIndex);
		FluidStack.STREAM_CODEC.encode(buffer, msg.contents);
	}

	public static PacketTankLevelUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketTankLevelUpdate(buffer.readBlockPos(), buffer.readVarInt(), FluidStack.STREAM_CODEC.decode(buffer));
	}

	public static void handle(PacketTankLevelUpdate msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, ILiquidTankTile.class, tile -> {
			ITankManager tankManager = tile.getTankManager();
			tankManager.processTankUpdate(msg.tankIndex, msg.contents);
		});
	}
}
