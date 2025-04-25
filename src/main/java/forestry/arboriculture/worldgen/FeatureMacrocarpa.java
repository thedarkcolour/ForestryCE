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

import java.util.HashSet;
import java.util.Set;

public class FeatureMacrocarpa extends FeatureTree {
	public FeatureMacrocarpa(ITreeGenData tree) {
		super(tree, 7, 7);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {

		//Direction d = FeatureHelper.DirectionHelper.getRandom(rand);
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int branchesEnd = 2;
		int y = height-3;

		Set<BlockPos> branches = new HashSet<>();
		while( y >= branchesEnd){
			int depth = height - y;
			branches.addAll(
					FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0,y,0), girth, 0.35f, 0.4f, (int)(depth/1.5f) + (int)Math.ceil(girth/2f), 2, 1)
			);
			y -= 3;
		}
		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		//Top of Tree
		float r = (girth/2f)+2.5f;
		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, height-1, girth/2), r, 2f, r, 1.2f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		//End of Branches
		for (BlockPos branchEnd: contour.getBranchEnds()){
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2f, 1.4f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

	}
}
