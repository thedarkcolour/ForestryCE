package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.IContainerSocketed;
import forestry.core.network.PacketIdServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSolderingIronClick(int slot) implements IForestryPacketServer {
	@Override
	public Type<?> type() {
		return PacketIdServer.SOLDERING_IRON_CLICK;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketSolderingIronClick msg) {
		buffer.writeVarInt(msg.slot);
	}

	public static PacketSolderingIronClick decode(RegistryFriendlyByteBuf buffer) {
		return new PacketSolderingIronClick(buffer.readVarInt());
	}

	public static void handle(PacketSolderingIronClick msg, IPayloadContext ctx) {
		Player player = ctx.player();

		if (player.containerMenu instanceof IContainerSocketed socketMenu) {
			ItemStack itemstack = player.containerMenu.getCarried();

			socketMenu.handleSolderingIronClickServer(msg.slot(), (ServerPlayer) player, itemstack);
		}
	}
}
