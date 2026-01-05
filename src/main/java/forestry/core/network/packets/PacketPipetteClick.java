package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.IContainerLiquidTanks;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record PacketPipetteClick(int slot) implements IForestryPacketServer {
	public static void handle(PacketPipetteClick msg, ServerPlayer player) {
		if (player.containerMenu instanceof IContainerLiquidTanks tanksMenu) {
			tanksMenu.handlePipetteClick(msg.slot(), player);
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.slot);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.PIPETTE_CLICK;
	}

	public static PacketPipetteClick decode(FriendlyByteBuf buffer) {
		return new PacketPipetteClick(buffer.readVarInt());
	}
}
