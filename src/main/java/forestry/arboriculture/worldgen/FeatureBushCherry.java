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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

public class FeatureBushCherry extends FeatureTree {
	public FeatureBushCherry(ITreeGenData tree) {
		super(tree, 4, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		Set<BlockPos> branchCoords = new HashSet<>();

		int branchHeight = height - 1;
		int branchWidth = height / 2;
		while (branchHeight > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchHeight, 0), girth, 0.2f, 0.5f, branchWidth, 1, 1.0f));
			branchHeight -= 2;
			branchWidth++;
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = height + 2;
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), girth, girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), girth, 1 + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCircle(level, rand, branchEnd.above(), 3, 3, 1, leaf, 1.0f, FeatureHelper.EnumReplaceMode.AIR, contour);
			FeatureHelper.generateCircle(level, rand, branchEnd, 4, 3, 1, leaf, 1.0f, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
