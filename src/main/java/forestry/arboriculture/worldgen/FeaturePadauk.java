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

public class FeaturePadauk extends FeatureTree {

	public FeaturePadauk(ITreeGenData tree) {
		super(tree, 6, 6);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		Set<BlockPos> branches = new HashSet<>();

		while (branches.size() < 3) {
			branches.addAll( FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, height-1, 0), girth, 0.35f, 0.2f, 8, 1, 2 ) );
		}

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		for (BlockPos branchEnd: contour.getBranchEnds()){

			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd.offset(0,1,0), 2, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 3, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		}

	}
}
