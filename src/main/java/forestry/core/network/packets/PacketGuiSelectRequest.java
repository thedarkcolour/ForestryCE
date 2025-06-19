package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.IGuiSelectable;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketGuiSelectRequest(int primaryIndex, int secondaryIndex) implements IForestryPacketServer {
	public static void handle(PacketGuiSelectRequest msg, IPayloadContext ctx) {
		AbstractContainerMenu container = ctx.player().containerMenu;

		if (container instanceof IGuiSelectable guiSelectable) {
			guiSelectable.handleSelectionRequest(ctx.player(), msg.primaryIndex(), msg.secondaryIndex());
		}
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.GUI_SELECTION_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketGuiSelectRequest msg) {
		buffer.writeVarInt(msg.primaryIndex);
		buffer.writeVarInt(msg.secondaryIndex);
	}

	public static PacketGuiSelectRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGuiSelectRequest(buffer.readVarInt(), buffer.readVarInt());
	}
}
