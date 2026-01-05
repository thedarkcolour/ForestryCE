package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.IGuiSelectable;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record PacketGuiSelectRequest(int primaryIndex, int secondaryIndex) implements IForestryPacketServer {
	public static void handle(PacketGuiSelectRequest msg, ServerPlayer player) {
		AbstractContainerMenu container = player.containerMenu;

		if (container instanceof IGuiSelectable guiSelectable) {
			guiSelectable.handleSelectionRequest(player, msg.primaryIndex(), msg.secondaryIndex());
		}
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.GUI_SELECTION_REQUEST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.primaryIndex);
		buffer.writeVarInt(this.secondaryIndex);
	}

	public static PacketGuiSelectRequest decode(FriendlyByteBuf buffer) {
		return new PacketGuiSelectRequest(buffer.readVarInt(), buffer.readVarInt());
	}
}
