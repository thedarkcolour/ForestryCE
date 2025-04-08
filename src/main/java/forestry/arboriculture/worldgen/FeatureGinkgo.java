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

public class FeatureGinkgo extends FeatureTree {
	public FeatureGinkgo(ITreeGenData tree) {
		super(tree, 7, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int trunkSpawn = height - 2;

		Set<BlockPos> branchCoords = new HashSet<>();
		while (trunkSpawn > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, trunkSpawn--, 0), girth, 0f, 0.3f, 2, 1, 0.5f));
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		//FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, 2, 0), girth, 2, 1.5f, height-1, FeatureHelper.EnumReplaceMode.AIR, contour);

		int leafSpawn = height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, height, 0), girth, 2f+(girth/2f), 1.25f, 1, FeatureHelper.EnumReplaceMode.AIR, contour);

		int range = (int)Math.ceil(girth/2f);
		int end = rand.nextIntBetweenInclusive(1,2);

		while (leafSpawn > end){

			int randX = rand.nextIntBetweenInclusive(-range,range);
			int randZ = rand.nextIntBetweenInclusive(-range,range);

			//larger trees get a bit more coverage
			for (int i = 0; i < Math.ceil(girth/2f); i++)
				FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset((girth/2)+randX, leafSpawn, (girth/2)+randZ), 3f, 1.25f, 1, FeatureHelper.EnumReplaceMode.AIR, contour);

			leafSpawn--;
		}

		for (BlockPos branchEnd: contour.getBranchEnds()){

			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 2.5f, 1.25f, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

	}
}
