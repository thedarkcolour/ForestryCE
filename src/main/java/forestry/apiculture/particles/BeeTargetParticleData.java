package forestry.apiculture.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BeeTargetParticleData(int entity, int color) implements ParticleOptions {
	public static final MapCodec<BeeTargetParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.INT.fieldOf("entity").forGetter(BeeTargetParticleData::entity),
		Codec.INT.fieldOf("color").forGetter(BeeTargetParticleData::color)
	).apply(instance, BeeTargetParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BeeTargetParticleData> STREAM_CODEC = StreamCodec.of(BeeTargetParticleData::encode, BeeTargetParticleData::decode);

	@Override
	public ParticleType<?> getType() {
		return ApicultureParticles.BEE_TARGET_ENTITY_PARTICLE.value();
	}

	private static void encode(RegistryFriendlyByteBuf buffer, BeeTargetParticleData msg) {
		buffer.writeInt(msg.entity);
		buffer.writeInt(msg.color);
	}

	private static BeeTargetParticleData decode(RegistryFriendlyByteBuf buffer) {
		return new BeeTargetParticleData(buffer.readInt(), buffer.readInt());
	}

	public static class Type extends ParticleType<BeeTargetParticleData> {
		protected Type() {
			super(false);
		}

		@Override
		public MapCodec<BeeTargetParticleData> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, BeeTargetParticleData> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
