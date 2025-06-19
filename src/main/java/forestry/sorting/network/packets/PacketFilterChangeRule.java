package forestry.sorting.network.packets;

import forestry.api.IForestryApi;
import forestry.api.genetics.filter.IFilterRuleType;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.sorting.FilterLogic;
import forestry.sorting.tiles.TileGeneticFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public record PacketFilterChangeRule(BlockPos pos, Direction facing,
									 IFilterRuleType rule) implements IForestryPacketServer {
	@Override
	public Type<?> type() {
		return PacketIdServer.FILTER_CHANGE_RULE;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketFilterChangeRule msg) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeShort(msg.facing.get3DDataValue());
		buffer.writeShort(IForestryApi.INSTANCE.getFilterManager().getId(msg.rule));
	}

	public static PacketFilterChangeRule decode(RegistryFriendlyByteBuf buffer) {
		return new PacketFilterChangeRule(buffer.readBlockPos(), Direction.VALUES[buffer.readShort()], Objects.requireNonNull(IForestryApi.INSTANCE.getFilterManager().getRule(buffer.readShort())));
	}

	public static void handle(PacketFilterChangeRule msg, IPayloadContext ctx) {
		Player player = ctx.player();
		if (player.level().getBlockEntity(msg.pos()) instanceof TileGeneticFilter filter) {
			FilterLogic logic = filter.getLogic();

			if (logic.setRule(msg.facing, msg.rule)) {
				logic.getNetworkHandler().sendToPlayers(logic, (ServerLevel) player.level(), player);
			}
		}
	}
}
