package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.IContainerSocketed;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PacketSolderingIronClick(int slot) implements IForestryPacketServer {
	@Override
	public ResourceLocation id() {
		return PacketIdServer.SOLDERING_IRON_CLICK;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.slot);
	}

	public static PacketSolderingIronClick decode(FriendlyByteBuf buffer) {
		return new PacketSolderingIronClick(buffer.readVarInt());
	}

	public static void handle(PacketSolderingIronClick msg, ServerPlayer player) {
		if (player.containerMenu instanceof IContainerSocketed socketMenu) {
			ItemStack itemstack = player.containerMenu.getCarried();

			socketMenu.handleSolderingIronClickServer(msg.slot(), player, itemstack);
		}
	}
}
