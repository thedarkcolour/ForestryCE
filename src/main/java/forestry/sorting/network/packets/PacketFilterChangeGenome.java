package forestry.sorting.network.packets;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import forestry.api.ForestryCapabilities;
import forestry.api.genetics.ISpecies;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SpeciesUtil;

public record PacketFilterChangeGenome(BlockPos pos, Direction facing, short index, boolean active, @Nullable ISpecies<?> species) implements IForestryPacketServer {
	public static void handle(PacketFilterChangeGenome msg, ServerPlayer player) {
		TileUtil.getInterface(player.level(), msg.pos(), ForestryCapabilities.FILTER_LOGIC, null).ifPresent(logic -> {
			if (logic.setGenomeFilter(msg.facing(), msg.index(), msg.active(), msg.species())) {
				logic.getNetworkHandler().sendToPlayers(logic, player.serverLevel(), player);
			}
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		NetworkUtil.writeDirection(buffer, this.facing);
		buffer.writeShort(this.index);
		buffer.writeBoolean(this.active);
		if (this.species != null) {
			buffer.writeBoolean(true);
			buffer.writeResourceLocation(this.species.id());
		} else {
			buffer.writeBoolean(false);
		}
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.FILTER_CHANGE_GENOME;
	}

	public static PacketFilterChangeGenome decode(FriendlyByteBuf buffer) {
		BlockPos pos = buffer.readBlockPos();
		Direction facing = NetworkUtil.readDirection(buffer);
		short index = buffer.readShort();
		boolean active = buffer.readBoolean();
		ISpecies<?> allele = buffer.readBoolean() ? SpeciesUtil.getAnySpecies(buffer.readResourceLocation()) : null;

		return new PacketFilterChangeGenome(pos, facing, index, active, allele);
	}
}
