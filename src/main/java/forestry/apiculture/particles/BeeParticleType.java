package forestry.apiculture.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

import javax.annotation.Nonnull;

public class BeeParticleType extends ParticleType<BeeParticleData> {
	public BeeParticleType() {
		super(false, BeeParticleData.DESERIALIZER);
	}

	@Nonnull
	@Override
	public Codec<BeeParticleData> codec() {
		return BeeParticleData.createCodec(this);
	}
}
