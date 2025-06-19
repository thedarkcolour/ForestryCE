package forestry.sorting.network.packets;

import forestry.api.ForestryCapabilities;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.sorting.AlleleFilter;
import forestry.sorting.FilterLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketGuiFilterUpdate(BlockPos pos, IFilterRuleType[] filterRules,
									AlleleFilter[][] genomeFilter) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.GUI_UPDATE_FILTER;
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		FilterLogic.writeFilterRules(buffer, this.filterRules);
		FilterLogic.writeGenomeFilters(buffer, this.genomeFilter);
	}

	public static PacketGuiFilterUpdate decode(RegistryFriendlyByteBuf buffer) {
		return new PacketGuiFilterUpdate(buffer.readBlockPos(), FilterLogic.readFilterRules(buffer), FilterLogic.readGenomeFilters(buffer));
	}

	public static void handle(PacketGuiFilterUpdate msg, IPayloadContext ctx) {
		TileUtil.getInterface(player.level(), msg.pos(), ForestryCapabilities.FILTER_LOGIC, null).ifPresent(l -> {
			if (l instanceof FilterLogic logic) {
				logic.readGuiUpdatePacket(msg);
			}
		});
	}
}
