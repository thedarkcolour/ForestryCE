package forestry.apiculture.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BeeParticleData(ParticleType<BeeParticleData> type, BlockPos destination, int color) implements ParticleOptions {
	@Override
	public ParticleType<?> getType() {
		return this.type;
	}

	public static class Type extends ParticleType<BeeParticleData> implements StreamCodec<RegistryFriendlyByteBuf, BeeParticleData> {
		private final MapCodec<BeeParticleData> codec;

		public Type() {
			super(false);

			this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BlockPos.CODEC.fieldOf("destination").forGetter(BeeParticleData::destination),
				Codec.INT.fieldOf("color").forGetter(BeeParticleData::color)
			).apply(instance, (destination, color) -> new BeeParticleData(this, destination, color)));
		}

		@Override
		public MapCodec<BeeParticleData> codec() {
			return this.codec;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, BeeParticleData> streamCodec() {
			return this;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buffer, BeeParticleData msg) {
			buffer.writeBlockPos(msg.destination);
			buffer.writeInt(msg.color);
		}

		@Override
		public BeeParticleData decode(RegistryFriendlyByteBuf buffer) {
			return new BeeParticleData(this, buffer.readBlockPos(), buffer.readInt());
		}
	}
}
