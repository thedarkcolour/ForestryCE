package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.features.CoreParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRefractoryWax(BlockPos pos) implements IForestryPacketClient {
	public Type<?> type() {
		return PacketIdClient.REFRACTORY_WAX_ON;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketRefractoryWax msg) {
		buffer.writeBlockPos(msg.pos);
	}

	public static PacketRefractoryWax decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRefractoryWax(buffer.readBlockPos());
	}

	public static void handle(PacketRefractoryWax msg, IPayloadContext ctx) {
		Level level = ctx.player().level();
		BlockPos pos = msg.pos;

		ParticleUtils.spawnParticlesOnBlockFaces(level, pos, CoreParticles.REFRACTORY_WAX.get(), UniformInt.of(3, 5));
		level.playLocalSound(pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1f, 1f, false);
	}
}
