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

public class FeatureGinkgo extends FeatureTree {
	public FeatureGinkgo(ITreeGenData tree) {
		super(tree, 7, 4);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int trunkSpawn = height - 1;
		float baseRad = 1f;
		float radMod = 2f;

		while (trunkSpawn > 2) {

			float radius =baseRad + (1.0f - (float)trunkSpawn / height) * radMod;
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn, 0), girth, 0.15f, 0.3f, (int)radius, 1, 0.25f));
			trunkSpawn-=3;
		}
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		int leafSpawn = height + 2;

		int end = rand.nextIntBetweenInclusive(1,2);
		int rAdd = 0;

		float baseRad = 1f;
		float radMod = 2f;

		while (leafSpawn > end){

			//Basically this makes a slightly conic cylinder, where the top is 2 blocks thinner than the base.
			float radius = baseRad + (1.0f - (float)leafSpawn / height) * radMod + rAdd + ((girth-1f)/2);

			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), girth, radius , 1.25f, 1, FeatureHelper.EnumReplaceMode.AIR, contour);

			rAdd = (rAdd == 0) ? 1 : 0;
		}

		for (BlockPos branchEnd: contour.getBranchEnds()){

			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2f, 1.25f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

	}
}
