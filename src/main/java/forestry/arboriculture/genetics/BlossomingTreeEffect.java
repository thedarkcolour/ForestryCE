package forestry.arboriculture.genetics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.genetics.IGenome;

public class BlossomingTreeEffect extends DummyTreeEffect {
	public BlossomingTreeEffect() {
		super(false);
	}

	@Override
	public void doAnimationEffect(IGenome genome, Level level, BlockPos pos, RandomSource rand) {
		if (rand.nextInt(10) == 0) {
			BlockPos below = pos.below();
			BlockState belowState = level.getBlockState(below);

			if (!Block.isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)) {
				ParticleUtils.spawnParticleBelow(level, pos, rand, ParticleTypes.CHERRY_LEAVES);
			}
		}
	}
}
