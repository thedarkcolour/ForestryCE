package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketActiveUpdate(BlockPos pos, boolean active) implements IForestryPacketClient {
	public PacketActiveUpdate(IActivatable tile) {
		this(tile.getCoordinates(), tile.isActive());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.TILE_FORESTRY_ACTIVE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeBoolean(this.active);
	}

	public static PacketActiveUpdate decode(FriendlyByteBuf buffer) {
		return new PacketActiveUpdate(buffer.readBlockPos(), buffer.readBoolean());
	}

	public static void handle(PacketActiveUpdate msg, Player player) {
		BlockEntity tile = TileUtil.getTile(player.level(), msg.pos);

		if (tile instanceof IActivatable activatable) {
			activatable.setActive(msg.active);
		} else if (tile instanceof IMultiblockComponent component) {
			if (component.getMultiblockLogic().isConnected() && component.getMultiblockLogic().getController() instanceof IActivatable activatable) {
				activatable.setActive(msg.active);
			}
		}
	}
}
