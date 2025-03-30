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

public class FeatureJacaranda extends FeatureTree {
	public FeatureJacaranda(ITreeGenData tree) {
		super(tree, 5, 2);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int trunkSpawn = height - 1;

		Set<BlockPos> branchCoords = new HashSet<>();
		while (trunkSpawn > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn-=1, 0), girth, 0.5f, 0.3f, height/2, 1, 0.5f));
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		float r = 2f+(float)Math.ceil(girth/1.5f);
		FeatureHelper.generateEllipsoid(level, startPos.offset(girth / 2, height-1, girth / 2), r, 2, r, 1.3f, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);

		for (BlockPos branchEnd: contour.getBranchEnds()){

			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2.5f, 1.25f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

	}
}
