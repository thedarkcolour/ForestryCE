package forestry.apiculture.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.multiblock.MultiblockValidation;
import forestry.core.network.PacketIdClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketAlvearyChange(BlockPos pos) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.ALVERAY_CONTROLLER_CHANGE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
	}

	public static PacketAlvearyChange decode(FriendlyByteBuf buffer) {
		return new PacketAlvearyChange(buffer.readBlockPos());
	}

	public static void handle(PacketAlvearyChange msg, Player player) {
		// Client-side re-validation (spec §5.3, §9): refresh the client's assembled state + entrance textures.
		MultiblockValidation.validateAt(player.level(), msg.pos);
	}
}
