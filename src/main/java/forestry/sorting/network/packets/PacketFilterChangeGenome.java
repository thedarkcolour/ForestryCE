package forestry.sorting.network.packets;

import forestry.api.genetics.ISpecies;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SpeciesUtil;
import forestry.sorting.FilterLogic;
import forestry.sorting.tiles.TileGeneticFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public record PacketFilterChangeGenome(BlockPos pos, Direction facing, short index, boolean active,
									   @Nullable ISpecies<?> species) implements IForestryPacketServer {
	public static void handle(PacketFilterChangeGenome msg, IPayloadContext ctx) {
		Player player = ctx.player();
		if (player.level().getBlockEntity(msg.pos()) instanceof TileGeneticFilter filter) {
			FilterLogic logic = filter.getLogic();

			if (logic.setGenomeFilter(msg.facing(), msg.index(), msg.active(), msg.species())) {
				logic.getNetworkHandler().sendToPlayers(logic, (ServerLevel) player.level(), player);
			}
		}
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketFilterChangeGenome msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeDirection(buffer, msg.facing);
		buffer.writeShort(msg.index);
		buffer.writeBoolean(msg.active);
		if (msg.species != null) {
			buffer.writeBoolean(true);
			buffer.writeResourceLocation(msg.species.id());
		} else {
			buffer.writeBoolean(false);
		}
	}

	@Override
	public Type<?> type() {
		return PacketIdServer.FILTER_CHANGE_GENOME;
	}

	public static PacketFilterChangeGenome decode(RegistryFriendlyByteBuf buffer) {
		BlockPos pos = buffer.readBlockPos();
		Direction facing = NetworkUtil.readDirection(buffer);
		short index = buffer.readShort();
		boolean active = buffer.readBoolean();
		ISpecies<?> allele = buffer.readBoolean() ? SpeciesUtil.getAnySpecies(buffer.readResourceLocation()) : null;

		return new PacketFilterChangeGenome(pos, facing, index, active, allele);
	}
}
