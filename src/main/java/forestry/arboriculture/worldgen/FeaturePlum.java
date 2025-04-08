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

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

import java.util.HashSet;
import java.util.Set;

public class FeaturePlum extends FeatureTree {


	public FeaturePlum(ITreeGenData tree) {
		super(tree, 5, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int branchSpawn = height - 2;
		int end = 2;

		float heightMult = Math.max(height / 4f, 1);
		float radius = heightMult + (girth / 2f); //give taller trees longer branches

		Set<BlockPos> branches = new HashSet<>();
		while (branchSpawn >= end) {

			branches.addAll(
					FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchSpawn, 0), girth, 0.4f, 0.15f, (int)radius, 1, 0.5f)
			);
			branchSpawn-=2;
		}

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		float bRadius = (float) Math.min(2, Math.ceil(girth/2f));
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, bRadius, 1.5f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height;

		int end = rand.nextIntBetweenInclusive(1, 2);
		float heightMult = Math.max(height/5f,1);
		int hGirth = (girth/2);

		while (leafSpawn >= end){
			int randX = rand.nextIntBetweenInclusive(-1, 1);
			int randZ = rand.nextIntBetweenInclusive(-1, 1);

			float radius = heightMult+hGirth+0.5f;

			if (leafSpawn == height || leafSpawn == end) {
				radius = Math.max(1, radius * 0.5f);
				randX = 0;
				randZ = 0;
			}
			FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset(hGirth + randX, leafSpawn, hGirth + randZ), radius, 1.5f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);

			leafSpawn--;
		}
	}
}
