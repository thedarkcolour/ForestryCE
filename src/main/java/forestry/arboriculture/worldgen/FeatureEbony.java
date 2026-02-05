package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class FeatureEbony extends FeatureTree {
	public FeatureEbony(ITreeGenData tree) {
		super(tree, 10, 4);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		int trunksGenerated = 0;

		// TODO: Anything but this
		for (int x = 0; x < this.girth; x++) {
			for (int z = 0; z < this.girth; z++) {
				if (rand.nextFloat() < 0.6f) {
					for (int y = 0; y < this.height; y++) {
						FeatureHelper.addBlock(level, startPos.offset(x, y, z), wood, FeatureHelper.EnumReplaceMode.ALL);
						if (y > this.height / 2 && rand.nextFloat() < 0.1f * (10f / this.height)) {
							break;
						}
					}
					trunksGenerated++;
				} else {
					for (int i = 0; i < 1; i++) {
						level.setBlock(startPos.offset(x, i, z), Blocks.AIR.defaultBlockState(), 18);
					}
				}
			}
		}

		// Generate backup trunk, if we failed to generate any.
		if (trunksGenerated <= 0) {
			FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, 1, 0, 0.6f, null, 0);
		}
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (int times = 0; times < 2 * this.height; times++) {
			int h = 2 * this.girth + rand.nextInt(this.height - this.girth);
			if (rand.nextBoolean() && h < this.height / 2) {
				h = this.height / 2 + rand.nextInt(this.height / 2);
			}

			int x_off = rand.nextInt(this.girth);
			int y_off = rand.nextInt(this.girth);

			BlockPos center = startPos.offset(x_off, h, y_off);
			int radius = 1 + rand.nextInt(this.girth);
			FeatureHelper.generateSphere(level, center, radius, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
