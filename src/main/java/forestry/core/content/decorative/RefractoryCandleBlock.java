package forestry.core.content.decorative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RefractoryCandleBlock extends CandleBlock {
	public RefractoryCandleBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(LIT)) {
			this.getParticleOffsets(state).forEach(v -> addParticlesAndSound(level, v.add(pos.getX(), pos.getY(), pos.getZ()), random));
		}
	}

	private static void addParticlesAndSound(Level level, Vec3 offset, RandomSource random) {
		float f = random.nextFloat();
		if (f < 0.3F) {
			level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
			if (f < 0.17F) {
				level.playLocalSound(offset.x + 0.5, offset.y + 0.5, offset.z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
			}
		}
		// reuse from torch, since there's no SMALL_SOUL_FLAME
		level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
	}
}
