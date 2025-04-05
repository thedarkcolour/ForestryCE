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
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

public class FeatureCoconut extends FeatureTree {
	public FeatureCoconut(ITreeGenData tree) {
		super(tree, 8, 4);
	}

	private static final Vec3i[] leafVectors = {
			//Top layer
			new Vec3i(1,1,1),
			new Vec3i(-1,1,1),
			new Vec3i(1,1,-1),
			new Vec3i(-1,1,-1),
			//Middle layer
			new Vec3i(1,0,0),
			new Vec3i(-1,0,0),
			new Vec3i(0,0,1),
			new Vec3i(0,0,-1),
			//Bottom layer
			new Vec3i(1,-1,1),
			new Vec3i(-1,-1,1),
			new Vec3i(1,-1,-1),
			new Vec3i(-1,-1,-1)
	};

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {

		Direction d = FeatureHelper.DirectionHelper.getRandom(rand);

		return FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, d, 3);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos sp) {
		BlockPos startPos = contour.getBranchEnds().get(0); //the ps parameter is not useful as it gives us the base of the tree :facepalm:

		int length = 3 + (girth/2);

		for (Vec3i v: leafVectors) {

			int dx = v.getX() * length;
			int dy = v.getY() * length;
			int dz = v.getZ() * length;

			float trueLength = dx*dx + dy*dy + dz*dz;
			float lengthMod = 1;
			//Truncate diagonal distances, to prevent decay
			if (trueLength > length*length) lengthMod *= 0.75f;

			BlockPos endPos = startPos.offset(
					(int)(dx * lengthMod),
					(int)(dy * lengthMod),
					(int)(dz * lengthMod)
			);

			FeatureHelper.generateLine(level,
				startPos,
				endPos,
				0.75f + (rand.nextFloat()/2) + ((float) (girth - 1) /2),
				1f,
				leaf,
				FeatureHelper.EnumReplaceMode.AIR,
				contour);


		}
	}
}
