package forestry.apiculture.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketAlvearyChange(BlockPos pos) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.ALVERAY_CONTROLLER_CHANGE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
	}

	public static PacketAlvearyChange decode(FriendlyByteBuf buffer) {
		return new PacketAlvearyChange(buffer.readBlockPos());
	}

	public static void handle(PacketAlvearyChange msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, IMultiblockComponent.class, tile -> tile.getMultiblockLogic().getController().reassemble());
	}
}
