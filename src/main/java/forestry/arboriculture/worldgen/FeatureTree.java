/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.worldgen;

import javax.annotation.Nullable;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.core.worldgen.FeatureHelper;

public abstract class FeatureTree extends FeatureArboriculture {
	private static final int minHeight = 4;
	private static final int maxHeight = 80;

	private final int baseHeight;
	private final int heightVariation;

	protected int girth;
	protected int height;

	protected FeatureTree(ITreeGenData tree, int baseHeight, int heightVariation) {
		super(tree);
		this.baseHeight = baseHeight;
		this.heightVariation = heightVariation;
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);
		return Set.of();
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafHeight = height + 1;
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), girth, girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), girth, 0.5f + girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight--, 0), girth, 1.9f + girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafHeight, 0), girth, 1.9f + girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
	}

	@Override
	protected void generateExtras(LevelAccessor level, RandomSource rand, BlockPos startPos) {
		if (hasPods()) {
			FeatureHelper.generatePods(tree, level, rand, startPos, height, minPodHeight, girth, FeatureHelper.EnumReplaceMode.AIR);
		}
	}

	@Override
	@Nullable
	public BlockPos getValidGrowthPos(LevelAccessor level, BlockPos pos) {
		return this.tree.getGrowthPos(this.tree.getDefaultGenome(), level, pos, this.girth, this.height);
	}

	@Override
	public final void preGenerate(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos startPos) {
		this.height = determineHeight(level, rand, this.baseHeight, this.heightVariation);
		this.girth = this.tree.getGirth(genome);
	}

	protected int modifyByHeight(LevelAccessor world, int val, int min, int max) {
		//ITreeModifier treeModifier = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(world);
		int determined = Math.round(val * tree.getHeightModifier(tree.getDefaultGenome()));/* * treeModifier.getHeightModifier(tree.getGenome(), 1f)*/
		return determined < min ? min : Math.min(determined, max);
	}

	protected int determineHeight(LevelAccessor world, RandomSource rand, int baseHeight, int heightVariation) {
		//ITreeModifier treeModifier = SpeciesUtil.TREE_TYPE.get().getTreekeepingMode(world);
		int height = baseHeight + rand.nextInt(heightVariation);
		int adjustedHeight = Math.round(height * tree.getHeightModifier(tree.getDefaultGenome()));/* * treeModifier.getHeightModifier(tree.getGenome(), 1f)*/
		return adjustedHeight < minHeight ? minHeight : Math.min(adjustedHeight, maxHeight);
	}
}
