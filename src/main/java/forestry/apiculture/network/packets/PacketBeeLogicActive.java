package forestry.apiculture.network.packets;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Similar to PacketGuiStream
public record PacketBeeLogicActive(
	BlockPos pos,
	// null on client side
	IBeekeepingLogic logic,
	// null on server side
	FriendlyByteBuf payload
) implements IForestryPacketClient {
	public PacketBeeLogicActive(IBeeHousing tile) {
		this(tile.getBlockPos(), tile.getBeekeepingLogic(), null);
	}

	@Override
	public Type<?> type() {
		return PacketIdClient.BEE_LOGIC_ACTIVE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketBeeLogicActive msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writePayloadBuffer(buffer, msg.logic::writeData);
	}

	public static PacketBeeLogicActive decode(RegistryFriendlyByteBuf buffer) {
		return new PacketBeeLogicActive(buffer.readBlockPos(), null, NetworkUtil.readPayloadBuffer(buffer));
	}

	public static void handle(PacketBeeLogicActive msg, IPayloadContext ctx) {
		IBeeHousing beeHousing = TileUtil.getTile(ctx.player().level(), msg.pos, IBeeHousing.class);
		if (beeHousing != null) {
			IBeekeepingLogic beekeepingLogic = beeHousing.getBeekeepingLogic();
			beekeepingLogic.readData(msg.payload);
		}
	}
}
