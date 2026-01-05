package forestry.core.worldgen;

import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class FeatureBase extends Feature<NoneFeatureConfiguration> {
	protected FeatureBase() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		return place(getDefaultGenome(), context.level(), context.random(), context.origin(), false);
	}

	public abstract IGenome getDefaultGenome();

	public abstract boolean place(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos pos, boolean forced);
}
