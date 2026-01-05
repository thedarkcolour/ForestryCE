package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.circuits.ItemCircuitBoard;
import forestry.core.gui.IContainerSocketed;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PacketChipsetClick(int slot) implements IForestryPacketServer {
	public static void handle(PacketChipsetClick msg, ServerPlayer player) {
		if (player.containerMenu instanceof IContainerSocketed socketMenu) {
			ItemStack itemstack = player.containerMenu.getCarried();
			// todo replace check with tag
			if (itemstack.getItem() instanceof ItemCircuitBoard) {
				socketMenu.handleChipsetClickServer(msg.slot(), player, itemstack);
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.slot);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.CHIPSET_CLICK;
	}

	public static PacketChipsetClick decode(FriendlyByteBuf buffer) {
		return new PacketChipsetClick(buffer.readVarInt());
	}
}
