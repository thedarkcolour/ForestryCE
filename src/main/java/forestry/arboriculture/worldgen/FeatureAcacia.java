package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.farming.HorizontalDirection;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class FeatureAcacia extends FeatureTree {
	public FeatureAcacia(ITreeGenData tree) {
		super(tree, 5, 4);
	}

	// For the record, this is terrible usage of this method, but hacky workarounds make the world go round.
	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		Direction firstDir = HorizontalDirection.VALUES.get(rand.nextIntBetweenInclusive(0, 3));
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, firstDir, (rand.nextFloat() * 3) + 0.5f);

		// Only generate a second trunk if it's in another direction
		Direction nextDir = HorizontalDirection.VALUES.get(rand.nextIntBetweenInclusive(0, 3));
		if (!firstDir.equals(nextDir)) {
			FeatureHelper.generateTreeTrunk(level, branchCoords, rand, wood, startPos, rand.nextIntBetweenInclusive(Math.max(2, this.height - 4), this.height), this.girth, 0, 0, nextDir, (rand.nextFloat() * 1.5f) + 0.5f);
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		// Generate the first, larger canopy
		BlockPos pos = contour.getTrunkOrigins().get(0);

		FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(this.girth / -2, 1, this.girth / -2), 2 + (this.girth / 2), 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(this.girth / -2, 0, this.girth / -2), 3 + (this.girth / 2), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		// Generate the second, if there is one
		if (!contour.getBranchEnds().isEmpty()) {
			pos = contour.getBranchEnds().get(0);

			FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(this.girth / -2, 1, this.girth / -2), 1 + (this.girth / 2), 2f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(this.girth / -2, 0, this.girth / -2), 2 + (this.girth / 2), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
