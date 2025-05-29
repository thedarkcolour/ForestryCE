package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IPodFruit;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.Set;

public abstract class FeatureTree extends FeatureArboriculture {
	private static final int minHeight = 4;
	private static final int maxHeight = 80;

	private final int baseHeight;
	private final int heightVariation;

	protected int girth;
	protected int height;

	protected FeatureTree(ITreeSpecies species, int baseHeight, int heightVariation) {
		super(species);
		this.baseHeight = baseHeight;
		this.heightVariation = heightVariation;
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		return Set.of();
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafHeight = this.height + 1;
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), this.girth, 0.5f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), this.girth, 1.9f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight, 0), this.girth, 1.9f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
	}

	@Override
	protected void generateExtras(LevelAccessor level, RandomSource rand, IGenome genome, BlockPos startPos) {
		if (genome.getActiveValue(TreeChromosomes.FRUIT) instanceof IPodFruit || genome.getInactiveValue(TreeChromosomes.FRUIT) instanceof IPodFruit) {
			FeatureHelper.generatePods(this.species, level, rand, startPos, this.height, minPodHeight, this.girth, FeatureHelper.EnumReplaceMode.AIR);
		}
	}

	@Override
	@Nullable
	public BlockPos getValidGrowthPos(LevelAccessor level, BlockPos pos) {
		return this.species.getGrowthPos(this.species.getDefaultGenome(), level, pos, this.girth, this.height);
	}

	@Override
	public final void preGenerate(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos startPos) {
		this.height = determineHeight(level, rand, genome, this.baseHeight, this.heightVariation);
		this.girth = this.species.getGirth(genome);
	}

	protected int modifyByHeight(LevelAccessor world, int val, int min, int max) {
		//ITreeModifier treeModifier = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(world);
		int determined = Math.round(val * this.species.getHeightModifier(this.species.getDefaultGenome()));/* * treeModifier.getHeightModifier(tree.getGenome(), 1f)*/
		return determined < min ? min : Math.min(determined, max);
	}

	protected int determineHeight(LevelAccessor world, RandomSource rand, IGenome genome, int baseHeight, int heightVariation) {
		//ITreeModifier treeModifier = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(world);
		int height = baseHeight + rand.nextInt(heightVariation);
		int adjustedHeight = Math.round(height * this.species.getHeightModifier(genome));/* * treeModifier.getHeightModifier(tree.getGenome(), 1f)*/
		return adjustedHeight < minHeight ? minHeight : Math.min(adjustedHeight, maxHeight);
	}
}
