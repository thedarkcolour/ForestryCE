package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.gui.TileMenu;
import forestry.core.network.PacketIdClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketGuiEnergy(int windowId, int value) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.GUI_ENERGY;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketGuiEnergy msg) {
		buffer.writeVarInt(msg.windowId);
		buffer.writeVarInt(msg.value);
	}

	public static PacketGuiEnergy decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGuiEnergy(buffer.readVarInt(), buffer.readVarInt());
	}

	public static void handle(PacketGuiEnergy msg, IPayloadContext ctx) {
		if (ctx.player().containerMenu.containerId == msg.windowId && ctx.player().containerMenu instanceof TileMenu<?> menu) {
			menu.onGuiEnergy(msg.value);
		}
	}
}
