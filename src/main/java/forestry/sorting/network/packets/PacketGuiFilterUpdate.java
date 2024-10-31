package forestry.sorting.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.ForestryCapabilities;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.sorting.AlleleFilter;
import forestry.sorting.FilterLogic;

public record PacketGuiFilterUpdate(BlockPos pos, IFilterRuleType[] filterRules, AlleleFilter[][] genomeFilter) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.GUI_UPDATE_FILTER;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		FilterLogic.writeFilterRules(buffer, filterRules);
		FilterLogic.writeGenomeFilters(buffer, genomeFilter);
	}

	public static PacketGuiFilterUpdate decode(FriendlyByteBuf buffer) {
		return new PacketGuiFilterUpdate(buffer.readBlockPos(), FilterLogic.readFilterRules(buffer), FilterLogic.readGenomeFilters(buffer));
	}

	public static void handle(PacketGuiFilterUpdate msg, Player player) {
		TileUtil.getInterface(player.level(), msg.pos(), ForestryCapabilities.FILTER_LOGIC, null).ifPresent(l -> {
			if (l instanceof FilterLogic logic) {
				logic.readGuiUpdatePacket(msg);
			}
		});
	}
}
