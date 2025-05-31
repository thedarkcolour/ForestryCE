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

public class FeatureFir extends FeatureTree {
	public FeatureFir(ITreeGenData tree) {
		super(tree, 7, 5);
	}
	private int MIN_HEIGHT = 3;

    @Override
	public void generateTrunk(LevelAccessor level, List<BlockPos> logOrigins, List<BlockPos> branchCoords, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {


		FeatureHelper.generateTreeTrunk(level, logOrigins, branchCoords, rand, wood, startPos, Math.max(height-girth, MIN_HEIGHT), girth, 0, 0, 0.4f);

		for (int yBranch = 3; yBranch < height - (height/2); yBranch++) {
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), girth, 0.05f, 0.1f, Math.round((height - yBranch) * 0.15f), 1, 0.33f));
		}
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateSphere(level, branchEnd, 2, leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height + girth + 1;

		float maxRadius = 2.25f + rand.nextFloat();
		maxRadius *= Math.min(1f, height/6f ); //Shrink the width of smaller trees

		//determines the rate of radius change as Y decreases.
		float step = maxRadius / height;
		float r = 0;

		//step *= (girth);
		int canopyHeight = rand.nextIntBetweenInclusive(1,2);

        while (leafSpawn > canopyHeight) {
			r += step;
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), girth, r, (4f/3), 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), girth, (r*0.75f),1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
