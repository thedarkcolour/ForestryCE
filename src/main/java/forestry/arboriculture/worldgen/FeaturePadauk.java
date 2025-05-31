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
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

public class FeaturePadauk extends FeatureTree {

	public FeaturePadauk(ITreeGenData tree) {
		super(tree, 6, 6);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, height, girth, 0, 0, null, 0);


		while (branchCoords.size() < 3) {
			branchCoords.addAll( FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, height-1, 0), girth, 0.35f, 0.2f, 8, 1, 2 ) );
		}

	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		for (BlockPos branchEnd: contour.getBranchEnds()){

			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd.offset(0,1,0), 2, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 3, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		}

	}
}
