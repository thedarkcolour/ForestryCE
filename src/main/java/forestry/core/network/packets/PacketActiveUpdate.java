package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketActiveUpdate(BlockPos pos, boolean active) implements IForestryPacketClient {
	public PacketActiveUpdate(IActivatable tile) {
		this(tile.getBlockPos(), tile.isActive());
	}

	public Type<?> type() {
		return PacketIdClient.ACTIVE_UPDATE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketActiveUpdate msg) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeBoolean(msg.active);
	}

	public static PacketActiveUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketActiveUpdate(buffer.readBlockPos(), buffer.readBoolean());
	}

	public static void handle(PacketActiveUpdate msg, IPayloadContext ctx) {
		BlockEntity tile = TileUtil.getTile(ctx.player().level(), msg.pos);

		if (tile instanceof IActivatable activatable) {
			activatable.setActive(msg.active);
		} else if (tile instanceof IMultiblockComponent component) {
			if (component.getMultiblockLogic().isConnected() && component.getMultiblockLogic().getController() instanceof IActivatable activatable) {
				activatable.setActive(msg.active);
			}
		}
	}
}
