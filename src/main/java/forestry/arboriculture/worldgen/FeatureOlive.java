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

public class FeatureOlive extends FeatureTree {
	public FeatureOlive(ITreeGenData tree) {
		super(tree, 3, 4, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int branchesEnd = 1;
		int y = height-1;

		Set<BlockPos> branches = new HashSet<>();
		while( y >= branchesEnd){
			branches.addAll(
					FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, y--, 0), girth, 0.225f, 0.25f, 2, 1, 0.25f)
			);
		}
		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		//Main Canopy

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, height,0), girth, 2+(int)(girth/2f), 1.0f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, height-1,0), girth, 2+(int)(girth/2f), 1.25f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);


		int leafSpawn = height-2;
		while (leafSpawn >= 2) {

			int randX = rand.nextIntBetweenInclusive(-1,1);
			int randY = rand.nextIntBetweenInclusive(-1,1);
			if (leafSpawn == height-1){
				randX = 0;
				randY = 0;
			}

			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(randX, leafSpawn,randY), girth, 2+(int)(girth/2f), 1.25f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);

			leafSpawn-=2;
		}

		//Branches
		for (BlockPos branchEnd: contour.getBranchEnds()){
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1f, 2f, 2, FeatureHelper.EnumReplaceMode.SOFT, contour);

		}
	}

	public void poop(){
		System.out.println("Hello, world!");
	}
}
