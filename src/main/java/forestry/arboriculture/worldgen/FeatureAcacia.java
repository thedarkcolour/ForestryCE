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
import forestry.api.farming.HorizontalDirection;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class FeatureAcacia extends FeatureTree {
	public FeatureAcacia(ITreeGenData tree) {
		super(tree, 5, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {

		Direction firstDir = HorizontalDirection.VALUES.get(rand.nextIntBetweenInclusive(0,3));

		Set<BlockPos> branches = new HashSet<>();

		branches.addAll(FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, firstDir, (rand.nextFloat()*3)+0.5f));


		//Only generate a second trunk if it's in another direction
		Direction nextDir = HorizontalDirection.VALUES.get(rand.nextIntBetweenInclusive(0,3));
		if (!firstDir.equals(nextDir)){
			branches.addAll(FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, rand.nextIntBetweenInclusive(Math.max(2, height-4), height), girth, 0, 0, nextDir, (rand.nextFloat()*2)+1f));
		}

		return branches;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		//Generate the first, larger canopy
		BlockPos pos = contour.getBranchEnds().get(0);

		FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(girth/-2,1,girth/-2), 2+(girth/2), 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(girth/-2,0,girth/-2), 3+(girth/2), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);


		//Generate the second, if there is one
		if (contour.getBranchEnds().size() > girth*girth ) {

			pos = contour.getBranchEnds().get( girth*girth );

			FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(girth/-2,1,girth/-2), 1+(girth/2), 2f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromPos(level, leaf, pos.offset(girth/-2,0,girth/-2), 2+(girth/2), 1.5f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		}


	}
}
