package forestry.apiculture.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.core.utils.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Locale;

public class BeeParticleData implements ParticleOptions {

	public static final Deserializer<BeeParticleData> DESERIALIZER = new Deserializer<>() {
		@Nonnull
		@Override
		public BeeParticleData fromCommand(@Nonnull ParticleType<BeeParticleData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			long direction = reader.readLong();
			reader.expect(' ');
			int color = reader.readInt();
			return new BeeParticleData(type, direction, color);
		}

		@Override
		public BeeParticleData fromNetwork(@Nonnull ParticleType<BeeParticleData> type, FriendlyByteBuf buf) {
			return new BeeParticleData(type, buf.readLong(), buf.readInt());
		}
	};

	public static Codec<BeeParticleData> createCodec(ParticleType<BeeParticleData> type) {
		return RecordCodecBuilder.create(val -> val.group(Codec.LONG.fieldOf("direction").forGetter(data -> data.destination.asLong()), Codec.INT.fieldOf("color").forGetter(data -> data.color)).apply(val, (destination1, color1) -> new BeeParticleData(type, destination1, color1)));
	}

	public final ParticleType<BeeParticleData> type;
	public final BlockPos destination;
	public final int color;

	public BeeParticleData(ParticleType<BeeParticleData> type, long destination, int color) {
		this.type = type;
		this.destination = BlockPos.of(destination);
		this.color = color;
	}

	public BeeParticleData(ParticleType<BeeParticleData> type, BlockPos destination, int color) {
		this.type = type;
		this.destination = destination;
		this.color = color;
	}

	@Nonnull
	@Override
	public ParticleType<?> getType() {
		return this.type;
	}

	@Override
	public void writeToNetwork(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeRegistryId(ForgeRegistries.PARTICLE_TYPES, this.type);
		buffer.writeLong(this.destination.asLong());
		buffer.writeInt(this.color);
	}

	@Nonnull
	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %d %d %d %d", ModUtil.getRegistryName(getType()), this.destination.getX(), this.destination.getY(), this.destination.getZ(), this.color);
	}
}
