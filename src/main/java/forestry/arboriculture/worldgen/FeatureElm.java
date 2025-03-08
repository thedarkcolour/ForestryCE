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

import forestry.Forestry;
import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.HashSet;
import java.util.Set;

public class FeatureElm extends FeatureTree {

	public FeatureElm(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int trunkSpawn = height - 3;
		float adjustedGirth = girth * .75f;

		Set<BlockPos> branchCoords = new HashSet<>();
		while (trunkSpawn > 3) {
			int radius = Math.round(adjustedGirth * (height - trunkSpawn) / 1.1f);
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn, 0), girth, 0.2f, 0.3f, radius, 1, 1.0f));
			trunkSpawn -= 2;
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = height + 1;
		float adjustedGirth = girth * .75f;

		FeatureHelper.generateEllipsoid(level, startPos.offset(0, leafSpawn-=2, 0), girth * 3.25f, 2, girth * 3.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );
		FeatureHelper.generateEllipsoid(level, startPos.offset(0, leafSpawn-=2, 0), girth * 4.875f, 2, girth * 4.875f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd, 3f * girth, 3f * adjustedGirth, 3f * girth, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(0,-(int)(3f * adjustedGirth)/2,0), 4.9f * girth, 2.5f * adjustedGirth, 4.9f * girth, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour );
		}
	}
}
