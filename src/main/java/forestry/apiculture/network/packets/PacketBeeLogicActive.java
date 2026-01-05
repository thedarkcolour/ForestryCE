package forestry.apiculture.network.packets;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

// Similar to PacketGuiStream
public record PacketBeeLogicActive(
	BlockPos pos,
	// null on client side
	IBeekeepingLogic logic,
	// null on server side
	FriendlyByteBuf payload
) implements IForestryPacketClient {
	public PacketBeeLogicActive(IBeeHousing tile) {
		this(tile.getCoordinates(), tile.getBeekeepingLogic(), null);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.BEE_LOGIC_ACTIVE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		NetworkUtil.writePayloadBuffer(buffer, this.logic::writeData);
	}

	public static PacketBeeLogicActive decode(FriendlyByteBuf buffer) {
		return new PacketBeeLogicActive(buffer.readBlockPos(), null, NetworkUtil.readPayloadBuffer(buffer));
	}

	public static void handle(PacketBeeLogicActive msg, Player player) {
		IBeeHousing beeHousing = TileUtil.getTile(player.level(), msg.pos, IBeeHousing.class);
		if (beeHousing != null) {
			IBeekeepingLogic beekeepingLogic = beeHousing.getBeekeepingLogic();
			beekeepingLogic.readData(msg.payload);
		}
	}
}
