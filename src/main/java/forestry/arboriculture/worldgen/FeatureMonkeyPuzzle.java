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

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

public class FeatureMonkeyPuzzle extends FeatureTree {
	public FeatureMonkeyPuzzle(ITreeGenData tree) {
		super(tree, 8, 5);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);
		FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, height-1, 0), girth, 0f, 0.0f, 2, 4, 1.0f); //Supports the top canopy

		if (height > 8) {

			int branchY = height - rand.nextIntBetweenInclusive(5, 7);
			return FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchY, 0), girth, 0.4f, 0.25f, 2, 1, 1.0f);
		}
		return Set.of();
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		//Generate top-most blob
		for (int i = 2; i >= 0; i--) {

			//float radMult = 1.5f-(i/2f);
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, (i-2)+height,0), girth, 5f-i, 1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		//Generate smaller blob for branches
		for( BlockPos branchEnd: contour.getBranchEnds()){
			for (int i = 2; i >= 0; i--) {
				FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd.offset(0,i,0), 2.5f-i, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			}
		}

	}
}
