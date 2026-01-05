package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.gui.ContainerTile;
import forestry.core.network.PacketIdClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketGuiEnergy(int windowId, int value) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.GUI_ENERGY;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.windowId);
		buffer.writeVarInt(this.value);
	}

	public static PacketGuiEnergy decode(FriendlyByteBuf buffer) {
		return new PacketGuiEnergy(buffer.readVarInt(), buffer.readVarInt());
	}

	public static void handle(PacketGuiEnergy msg, Player player) {
		if (player.containerMenu.containerId == msg.windowId && player.containerMenu instanceof ContainerTile<?> menu) {
			menu.onGuiEnergy(msg.value);
		}
	}
}
