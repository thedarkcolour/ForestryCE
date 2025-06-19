package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.circuits.ItemCircuitBoard;
import forestry.core.gui.IContainerSocketed;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketChipsetClick(int slot) implements IForestryPacketServer {
	public static void handle(PacketChipsetClick msg, IPayloadContext ctx) {
		Player player = ctx.player();
		if (player.containerMenu instanceof IContainerSocketed socketMenu) {
			ItemStack itemstack = player.containerMenu.getCarried();
			if (itemstack.getItem() instanceof ItemCircuitBoard) {
				socketMenu.handleChipsetClickServer(msg.slot(), (ServerPlayer) player, itemstack);
			}
		}
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketChipsetClick msg) {
		buffer.writeVarInt(msg.slot);
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.CHIPSET_CLICK;
	}

	public static PacketChipsetClick decode(RegistryFriendlyByteBuf buffer) {
		return new PacketChipsetClick(buffer.readVarInt());
	}
}
