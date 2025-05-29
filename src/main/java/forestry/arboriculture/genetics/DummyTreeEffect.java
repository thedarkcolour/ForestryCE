package forestry.arboriculture.genetics;

import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class DummyTreeEffect implements ITreeEffect {
	private final boolean dominant;

	public DummyTreeEffect(boolean dominant) {
		this.dominant = dominant;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

	@Override
	public IEffectData validateStorage(IEffectData storedData) {
		return storedData;
	}

	@Override
	public IEffectData doEffect(IGenome genome, IEffectData storedData, Level level, BlockPos pos) {
		return storedData;
	}

	@Override
	public void doAnimationEffect(IGenome genome, Level level, BlockPos pos, RandomSource rand) {
	}
}
