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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashSet;
import java.util.Set;

public class FeatureCoconut extends FeatureTree {
	public FeatureCoconut(ITreeGenData tree) {
		super(tree, 10, 4);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {

		Direction d = Direction.getRandom(rand);

		return FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, girth, 0, 0, d, 3);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (Direction dir: Direction.VALUES){
			int length = 3 + rand.nextInt(3); // Frond length varies between 3-5 blocks
			BlockPos leafPos = startPos;

			for (int i = 0; i < length; i++) {
				leafPos = leafPos.offset(dir.getStepX(), dir.getStepY(), dir.getStepZ());
				FeatureHelper.addBlock(level, leafPos, leaf, FeatureHelper.EnumReplaceMode.SOFT, contour);

				// Make the leaves slightly curved by gradually lowering them
				if (i % 2 == 0) {
					leafPos.offset(0, -1, 0);
				}
			}
		}
	}
}
