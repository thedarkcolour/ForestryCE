package forestry.apiculture.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketAlvearyChange(BlockPos pos) implements IForestryPacketClient {
	@Override
	public Type<?> type() {
		return PacketIdClient.ALVEARY_CONTROLLER_CHANGE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketAlvearyChange msg) {
		buffer.writeBlockPos(msg.pos);
	}

	public static PacketAlvearyChange decode(RegistryFriendlyByteBuf buffer) {
		return new PacketAlvearyChange(buffer.readBlockPos());
	}

	public static void handle(PacketAlvearyChange msg, IPayloadContext ctx) {
		TileUtil.actOnTile(ctx.player().level(), msg.pos, IMultiblockComponent.class, tile -> tile.getMultiblockLogic().getController().reassemble());
	}
}
