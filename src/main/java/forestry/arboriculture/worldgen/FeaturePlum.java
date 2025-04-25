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
		super(tree, 4, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

		Set<BlockPos> branchCoords = new HashSet<>();

		int branchHeight = height - 1;
		int branchWidth = height / 4;
		while (branchHeight > 2) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchHeight, 0), girth, 0.2f, 0.5f, branchWidth, 1, 1.0f));
			branchHeight -= 2;
			//branchWidth++;
			//first (top-most) set of branches are shorter than the rest
			branchWidth = height / 2;
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = height+2 ;

		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, leafSpawn-=2, girth/2), (girth/2f)+2, 1.5f,  (girth/2f) + 2, 1.5f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int dx = 0;
		int dz = 0;

		while (leafSpawn > 4) {

			FeatureHelper.generateEllipsoid(level, startPos.offset((girth/2)+dx, leafSpawn-=2, (girth/2)+dz), (girth/2f)+3, 1.5f,  (girth/2f) + 3, 1.25f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

			dx = rand.nextIntBetweenInclusive(-1, 1);
			dz = rand.nextIntBetweenInclusive(-1, 1);

		} ;


		for (BlockPos branchEnd : contour.getBranchEnds()) {

			FeatureHelper.generateEllipsoid(level, branchEnd, 2, 1.5f,  2, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
