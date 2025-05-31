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

public class FeatureFeijoa extends FeatureTree {

	public FeatureFeijoa(ITreeGenData tree) {
		super(tree, 4, 1, 2);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, height, girth, 0, 0, null, 0);

		float chance = 0.75f;
		if (height >= 3) {
			chance = 0.5f;
		}

		for (int y = height - 1; y >= 1; y--){
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, y, 0), girth, 0, 0.25f, girth/3, 2, chance));
		}
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1.5f, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height;

		int radius = (int)Math.ceil((float)girth/2)+1;
		do {
			FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, leafSpawn--, girth/2), radius, 1f, radius, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		} while (leafSpawn > 2);
	}
}
