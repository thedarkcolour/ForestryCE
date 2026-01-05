package forestry.apiculture.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketHabitatBiomePointer(BlockPos pos) implements IForestryPacketClient {
	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.HABITAT_BIOME_POINTER;
	}

	public static PacketHabitatBiomePointer decode(FriendlyByteBuf buffer) {
		return new PacketHabitatBiomePointer(buffer.readBlockPos());
	}

	public static void handle(PacketHabitatBiomePointer msg, Player player) {
		BlockPos pos = msg.pos();
		//TextureHabitatLocator.getInstance().setTargetCoordinates(pos);//TODO: TextureHabitatLocator
	}
}
