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
import java.util.List;
import java.util.Set;

public class FeatureElm extends FeatureTree {

	public FeatureElm(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int trunkSpawn = height - 3;
		float adjustedGirth = girth * .75f;

		while (trunkSpawn > 3) {
			int radius = (int) Math.round(adjustedGirth + (height - trunkSpawn) * 1.2 );
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn, 0), girth, 0.2f, 0.3f, radius, 1, 0.85f));
			trunkSpawn -= 2;
		}
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = height + 1;
		float adjustedGirth = girth * .75f;

		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, leafSpawn-=1, girth/2), girth + 2.25f, 2, girth + 2.25f, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );
		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, leafSpawn-=2, girth/2), girth + 3.875f, 2.5f, girth + 3.875f, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd, 2f + girth, 2, 2f + girth, .9f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(0,-2,0), 3f + girth, 2, 3f + girth, .9f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );
		}
	}
}
