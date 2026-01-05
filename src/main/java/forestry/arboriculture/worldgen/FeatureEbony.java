package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.Set;

public class FeatureEbony extends FeatureTree {

	public FeatureEbony(ITreeGenData tree) {
		super(tree, 10, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		int trunksGenerated = 0;

		for (int x = 0; x < this.girth; x++) {
			for (int z = 0; z < this.girth; z++) {
				if (rand.nextFloat() < 0.6f) {
					for (int y = 0; y < this.height; y++) {
						FeatureHelper.addBlock(level, startPos.offset(x, y, z), wood, FeatureHelper.EnumReplaceMode.ALL);
						if (y > this.height / 2 && rand.nextFloat() < 0.1f * (10 / this.height)) {
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
			FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, 1, 0, 0.6f, null, 0);
		}

		return Collections.emptySet();
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
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
