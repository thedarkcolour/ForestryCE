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

public class FeatureBeech extends FeatureTree {
	public FeatureBeech(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		Set<BlockPos> branchCoords = new HashSet<>(FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0));

		int radius = Math.round((float)girth/5 + 1.5f);

		int maxBranchHeight = rand.nextInt(3,5);

		for (int yBranch = height - 1; yBranch > maxBranchHeight ; yBranch--) {
			branchCoords.addAll(
				FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), girth, 0.15f, 0.25f, radius, 1, 0.5f)
			);
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int r = 3;
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateEllipsoid(level, branchEnd.offset(girth/2, -1, girth/2), r, 2, r, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		int yCenter = height - girth;
		yCenter = yCenter > 3 ? yCenter : 4;
		FeatureHelper.generateEllipsoid(level, startPos.offset(0, yCenter, 0), r, 3 + rand.nextInt(height/3), r, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
