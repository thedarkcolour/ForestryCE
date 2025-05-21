package forestry.core.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.particles.CoreParticles;

public record PacketRefractoryWax(BlockPos pos) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.REFRACTORY_WAX_ON;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
	}

	public static PacketRefractoryWax decode(FriendlyByteBuf buffer) {
		return new PacketRefractoryWax(buffer.readBlockPos());
	}

	public static void handle(PacketRefractoryWax msg, Player player) {
		Level level = player.level();
		BlockPos pos = msg.pos;

		ParticleUtils.spawnParticlesOnBlockFaces(level, pos, CoreParticles.REFRACTORY_WAX.get(), UniformInt.of(3, 5));
		level.playLocalSound(pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1f, 1f, false);
	}
}
