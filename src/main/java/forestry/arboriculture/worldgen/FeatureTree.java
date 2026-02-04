package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

public abstract class FeatureTree extends FeatureArboriculture {
	private final int minHeight;
	private final int maxHeight = 80;

	private final int baseHeight;
	private final int heightVariation;

	protected int girth;
	protected int height;

	protected FeatureTree(ITreeGenData tree, int baseHeight, int heightVariation, int minHeightOverride) {
		super(tree);
		this.baseHeight = baseHeight;
		this.heightVariation = heightVariation;
		this.minHeight = minHeightOverride;
	}

	protected FeatureTree(ITreeGenData tree, int baseHeight, int heightVariation) {
		super(tree);
		this.baseHeight = baseHeight;
		this.heightVariation = heightVariation;
		this.minHeight = 4;
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
	}

	@Override
	protected void generateLeaves(IGenome genome, LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafHeight = this.height + 1;
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), this.girth, 0.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), this.girth, 1.9f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight, 0), this.girth, 1.9f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
	}

	@Override
	protected void generateExtras(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos startPos, TreeContour contour) {
		if (genome.getActiveValue(TreeChromosomes.FRUIT).requiresFruitBlocks()) {
			FeatureHelper.generatePods(genome, level, rand, startPos, this.height, minPodHeight, this.girth, contour, FeatureHelper.EnumReplaceMode.AIR);
		}
	}

	@Override
	@Nullable
	public BlockPos getValidGrowthPos(LevelAccessor level, BlockPos pos) {
		return this.tree.getGrowthPos(this.tree.getDefaultGenome(), level, pos, this.girth, this.height);
	}

	@Override
	public final void preGenerate(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos startPos) {
		this.height = determineHeight(level, rand, genome, this.baseHeight, this.heightVariation);
		this.girth = this.tree.getGirth(genome);
	}

	protected int modifyByHeight(LevelAccessor world, int val, int min, int max) {
		//ITreeModifier treeModifier = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(world);
		int determined = Math.round(val * this.tree.getHeightModifier(this.tree.getDefaultGenome()));/* * treeModifier.getHeightModifier(tree.getGenome(), 1f)*/
		return determined < min ? min : Math.min(determined, max);
	}

	protected int determineHeight(LevelAccessor world, RandomSource rand, IGenome genome, int baseHeight, int heightVariation) {
		//ITreeModifier treeModifier = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(world);
		int height = baseHeight + rand.nextInt(heightVariation);
		int adjustedHeight = Math.round(height * this.tree.getHeightModifier(genome));/* * treeModifier.getHeightModifier(tree.getGenome(), 1f)*/
		return adjustedHeight < this.minHeight ? this.minHeight : Math.min(adjustedHeight, this.maxHeight);
	}
}
