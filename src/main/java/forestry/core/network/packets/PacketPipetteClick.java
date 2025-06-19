package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.IContainerLiquidTanks;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPipetteClick(int slot) implements IForestryPacketServer {
	public static void handle(PacketPipetteClick msg, IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof IContainerLiquidTanks tanksMenu) {
			tanksMenu.handlePipetteClick(msg.slot(), (ServerPlayer) ctx.player());
		}
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketPipetteClick msg) {
		buffer.writeVarInt(msg.slot);
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.PIPETTE_CLICK;
	}

	public static PacketPipetteClick decode(RegistryFriendlyByteBuf buffer) {
		return new PacketPipetteClick(buffer.readVarInt());
	}
}
