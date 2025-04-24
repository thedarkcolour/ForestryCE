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

public class FeatureKauri extends FeatureTree {
	public FeatureKauri(ITreeGenData tree) {
		super(tree, 15, 5);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {

		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);
		FeatureHelper.generateSupportStems(wood, level, rand, startPos, height, girth, 0.8f, 0.2f);

		Set<BlockPos> branchPositions = new HashSet<>();

		int count = rand.nextIntBetweenInclusive((int)(girth *4.5f), (int)(girth * 6.5f));
		int branchWidth = (int)(height / 2.5f);


		while (branchPositions.size() <= count){

			//Make a nest of branches at the top of the tree. Account for very small trees.
			int branchPos = rand.nextIntBetweenInclusive(Math.max(height-8, 2), height);

			//branches closer to the top tend to climb upward more
			float spreadMod = 0.2f * (branchPos / (float) height);

			branchPositions.addAll( FeatureHelper.generateSmartBranches( level, rand, wood, startPos.offset(0,branchPos,0), girth, 0.2f + spreadMod, 0.4f, branchWidth, 1, 0.5f ) );

		}

		return branchPositions;

	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		for (BlockPos blockPos: contour.getBranchEnds()){

			FeatureHelper.generateEllipsoid(level, blockPos.offset(0, 1, 0), 2, 2, 2, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, blockPos.offset(0, -1, 0), 2, 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		}


		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, height+1, girth/2), 2+(girth/2f), 2, 2+(girth/2f), 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromPos(level, leaf, startPos.offset(girth/2, height-1, girth/2), 2+(girth/2f), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);


	}
}
