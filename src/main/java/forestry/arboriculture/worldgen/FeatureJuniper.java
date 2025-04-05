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

public class FeatureJuniper extends FeatureTree {
	public FeatureJuniper(ITreeGenData tree) {
		super(tree, 4, 2, 2);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {

		return FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, Math.max(height-girth, 2), girth, 0, 0, 0.4f);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		int leafSpawn = height + 3;

		//float heightMult = Math.max(height/8f,1);
		//float maxRadius = Math.min(6, rand.nextIntBetweenInclusive(1,3)*heightMult);
		float maxRadius = 1.5f + rand.nextFloat() + (girth/2);

		//determines the rate of radius change as Y decreases.
		float step = maxRadius / leafSpawn;
		float r = 0;

		//step *= (girth);
		int canopyHeight = rand.nextIntBetweenInclusive(0,1);

		while (leafSpawn >= canopyHeight) {
			r += step;
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), girth, r, (4f/3), 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
		if (canopyHeight == 1)
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), girth, (r*0.75f),1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
