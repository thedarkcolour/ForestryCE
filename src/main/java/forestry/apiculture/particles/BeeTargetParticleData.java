package forestry.apiculture.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.core.utils.ModUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Locale;

public class BeeTargetParticleData implements ParticleOptions {

	public static final Deserializer<BeeTargetParticleData> DESERIALIZER = new Deserializer<>() {
		@Nonnull
		@Override
		public BeeTargetParticleData fromCommand(@Nonnull ParticleType<BeeTargetParticleData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			int entityId = reader.readInt();
			reader.expect(' ');
			int color = reader.readInt();
			return new BeeTargetParticleData(entityId, color);
		}

		@Override
		public BeeTargetParticleData fromNetwork(@Nonnull ParticleType<BeeTargetParticleData> type, FriendlyByteBuf buf) {
			return new BeeTargetParticleData(buf.readInt(), buf.readInt());
		}
	};

	public static Codec<BeeTargetParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("entity").forGetter(data -> data.entity),
		Codec.INT.fieldOf("color").forGetter(data -> data.color)
	).apply(instance, BeeTargetParticleData::new));

	public final int entity;
	public final int color;

	public BeeTargetParticleData(int entity, int color) {
		this.entity = entity;
		this.color = color;
	}

	public BeeTargetParticleData(Entity entity, int color) {
		this.entity = entity.getId();
		this.color = color;
	}

	@Nonnull
	@Override
	public ParticleType<?> getType() {
		return ApicultureParticles.BEE_TARGET_ENTITY_PARTICLE.get();
	}

	@Override
	public void writeToNetwork(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.entity);
		buffer.writeInt(this.color);
	}

	@Nonnull
	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %d %d", ModUtil.getRegistryName(getType()), this.entity, this.color);
	}
}
