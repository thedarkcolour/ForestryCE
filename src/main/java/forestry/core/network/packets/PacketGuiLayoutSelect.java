package forestry.core.network.packets;

import forestry.api.IForestryApi;
import forestry.api.circuits.ICircuitLayout;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.circuits.ContainerSolderingIron;
import forestry.core.network.PacketIdClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketGuiLayoutSelect(String layoutUid) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.GUI_LAYOUT_SELECT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.layoutUid);
	}

	public static PacketGuiLayoutSelect decode(FriendlyByteBuf buffer) {
		return new PacketGuiLayoutSelect(buffer.readUtf());
	}

	public static void handle(PacketGuiLayoutSelect msg, Player player) {
		if (player.containerMenu instanceof ContainerSolderingIron solderingIron) {
			ICircuitLayout layout = IForestryApi.INSTANCE.getCircuitManager().getLayout(msg.layoutUid);

			if (layout != null) {
				solderingIron.setLayout(layout);
			}
		}
	}
}
