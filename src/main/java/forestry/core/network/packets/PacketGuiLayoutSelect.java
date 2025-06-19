package forestry.core.network.packets;

import forestry.api.IForestryApi;
import forestry.api.circuits.CircuitLayout;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.circuits.SolderingIronMenu;
import forestry.core.network.PacketIdClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketGuiLayoutSelect(String layoutUid) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.GUI_LAYOUT_SELECT;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketGuiLayoutSelect msg) {
		buffer.writeUtf(msg.layoutUid);
	}

	public static PacketGuiLayoutSelect decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGuiLayoutSelect(buffer.readUtf());
	}

	public static void handle(PacketGuiLayoutSelect msg, IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof SolderingIronMenu solderingIron) {
			CircuitLayout layout = IForestryApi.INSTANCE.getCircuitManager().getLayout(msg.layoutUid);

			if (layout != null) {
				solderingIron.setLayout(layout);
			}
		}
	}
}
