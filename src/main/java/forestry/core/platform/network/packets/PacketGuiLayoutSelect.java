package forestry.core.platform.network.packets;

import forestry.api.IForestryApi;
import forestry.api.core.circuits.ICircuitLayout;
import forestry.core.engine.circuits.SolderingIronMenu;
import forestry.core.platform.network.PacketIdClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record PacketGuiLayoutSelect(String layoutUid) implements CustomPacketPayload {
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdClient.GUI_LAYOUT_SELECT;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketGuiLayoutSelect msg) {
		buffer.writeUtf(msg.layoutUid);
	}

	public static PacketGuiLayoutSelect decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGuiLayoutSelect(buffer.readUtf());
	}

	public static void handle(PacketGuiLayoutSelect msg, Player player) {
		if (player.containerMenu instanceof SolderingIronMenu solderingIron) {
			ICircuitLayout layout = IForestryApi.INSTANCE.getCircuitManager().getLayout(msg.layoutUid);

			if (layout != null) {
				solderingIron.setLayout(layout);
			}
		}
	}
}
