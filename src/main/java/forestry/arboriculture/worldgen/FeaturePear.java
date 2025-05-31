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

import java.util.List;
import java.util.Set;

public class FeaturePear extends FeatureTree {
	public FeaturePear(ITreeGenData tree) {
		super(tree, 5, 3, 3);
	}

	@Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, logOrigins, rand, wood, startPos, height, girth, 0, 0, null, 0);

		branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, (int)Math.max(height*0.4f,2), 0), girth, 0.15f, 0.15f, Math.round(height/5f), 2, 0.75f));
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1, 2f,2,  FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height-1;

		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, height-1, girth/2), girth, 2, girth, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int end = rand.nextIntBetweenInclusive(1, 2);
		float heightMult = Math.max(height/5f,1);
		float radius = heightMult+(girth/2f); //give taller trees thicker foliage

		while (leafSpawn >= end){
			int randX = rand.nextIntBetweenInclusive(-1, 1);
			int randZ = rand.nextIntBetweenInclusive(-1, 1);

			if (leafSpawn == height-1 || leafSpawn == end){
				randX = 0;
				randZ = 0;
			}


			FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset((girth/2)+randX, leafSpawn, (girth/2)+randZ), radius, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

			leafSpawn--;
		}
	}
}
