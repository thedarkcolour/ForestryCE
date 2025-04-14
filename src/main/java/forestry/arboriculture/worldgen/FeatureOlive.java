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
		super(tree, 5, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		return FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, null, 0);

	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {

		float radius = 2+(int)(girth/2f);

		//Main Canopy
		FeatureHelper.generateEllipsoid(level, startPos.offset(girth/2, height, girth/2), radius, 1, radius, 1.75f, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);


		int leafSpawn = height-1;
		int i = 0;

		while (leafSpawn >= 2) {

			int gMod = Math.round(girth/2f); //omg garry's modification reference????

			int randX = rand.nextIntBetweenInclusive(-gMod,gMod);
			int randZ = rand.nextIntBetweenInclusive(-gMod,gMod);

			if (leafSpawn == 2){
				randX /= 2;
				randZ /= 2;
			}

			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(randX, leafSpawn,randZ), girth, radius, 1.25f, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

			if (i >= (girth/2 + 1)){
				i = 0;
				leafSpawn--;
			}
			else
				i++;
		}
	}
}
